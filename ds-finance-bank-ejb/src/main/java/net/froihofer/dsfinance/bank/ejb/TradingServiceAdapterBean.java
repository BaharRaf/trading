package net.froihofer.dsfinance.bank.ejb;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.xml.ws.BindingProvider;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;
import net.froihofer.dsfinance.ws.trading.api.PublicStockQuote;
import net.froihofer.dsfinance.ws.trading.api.TradingWSException_Exception;
import net.froihofer.dsfinance.ws.trading.api.TradingWebService;
import net.froihofer.dsfinance.ws.trading.api.TradingWebServiceService;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import net.froihofer.dsfinance.bank.entity.StockEntity;


@Stateless
@RolesAllowed({"employee", "customer"})
public class TradingServiceAdapterBean {

    @PersistenceContext
    private EntityManager em;


    private static final Logger LOG = LoggerFactory.getLogger(TradingServiceAdapterBean.class);

    // System properties (set on the WildFly JVM)
    private static final String PROP_USER = "trading.ws.user";
    private static final String PROP_PASS = "trading.ws.pass";
    private static final String PROP_ENDPOINT = "trading.ws.endpoint";

    // Default endpoint (SOAP address, NOT ?wsdl)
    private static final String DEFAULT_ENDPOINT =
            "https://edu.dedisys.org/ds-finance/ws/TradingService";

    // Recommended: place the WSDL into resources so build+runtime do NOT depend on fetching the WSDL over HTTP.
    // Path: ds-finance-bank-ejb/src/main/resources/META-INF/wsdl-consumed/TradingService.wsdl
    private static final String CLASSPATH_WSDL = "META-INF/wsdl-consumed/TradingService.wsdl";

    private volatile TradingWebService port;

    public List<StockQuoteDTO> findStockQuotesByCompanyName(String companyNameQuery) {
        String q = (companyNameQuery == null) ? "" : companyNameQuery.trim();

        try {
            List<PublicStockQuote> quotes = getPort().findStockQuotesByCompanyName(q);
            if (quotes == null) return List.of();

            List<StockQuoteDTO> out = new ArrayList<>(quotes.size());
            for (PublicStockQuote wsQuote : quotes) {
                out.add(toDto(wsQuote));
            }

            // Cache symbol -> companyName in DB (best effort)
            cacheStocks(out);

            return out;


        } catch (TradingWSException_Exception e) {
            LOG.warn("TradingService returned a domain error for query='{}': {}", q, e.getMessage());
            throw new RuntimeException("TradingService call failed: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("TradingService call failed for query='{}'", q, e);
            throw new RuntimeException("TradingService call failed: " + e.getMessage(), e);
        }
    }

    private void cacheStocks(List<StockQuoteDTO> quotes) {
        if (quotes == null || quotes.isEmpty()) return;

        for (StockQuoteDTO q : quotes) {
            if (q == null) continue;

            String symbol = normalizeSymbol(q.getSymbol());
            if (symbol == null) continue;

            String companyName = (q.getCompanyName() != null) ? q.getCompanyName().trim() : null;

            try {
                StockEntity existing = em.createQuery(
                                "SELECT s FROM StockEntity s WHERE s.symbol = :symbol", StockEntity.class)
                        .setParameter("symbol", symbol)
                        .getSingleResult();

                // Update companyName if we have a better one
                if (companyName != null && !companyName.isBlank()) {
                    if (existing.getCompanyName() == null || existing.getCompanyName().isBlank()) {
                        existing.setCompanyName(companyName);
                    }
                }
            } catch (NoResultException e) {
                // create new
                StockEntity stock = new StockEntity(symbol, companyName);
                em.persist(stock);
            } catch (Exception ignored) {
                // Best-effort only: never break the WS call due to caching issues
            }
        }
    }

    private String normalizeSymbol(String symbol) {
        if (symbol == null) return null;
        String s = symbol.trim();
        return s.isEmpty() ? null : s.toUpperCase(java.util.Locale.ROOT);
    }


    private TradingWebService getPort() {
        TradingWebService p = port;
        if (p != null) return p;

        synchronized (this) {
            if (port == null) {
                port = createAndConfigurePort();
            }
            return port;
        }
    }

    private TradingWebService createAndConfigurePort() {
        URL wsdl = Thread.currentThread().getContextClassLoader().getResource(CLASSPATH_WSDL);

        // If WSDL is packaged locally: use it. Otherwise: fall back to generated default URL.
        TradingWebServiceService service = (wsdl != null)
                ? new TradingWebServiceService(wsdl)
                : new TradingWebServiceService();

        TradingWebService p = service.getTradingWebServicePort();
        configureEndpointAndAuth(p);
        return p;
    }

    private void configureEndpointAndAuth(TradingWebService p) {
        String user = requiredSystemProperty(PROP_USER);
        String pass = requiredSystemProperty(PROP_PASS);
        String endpoint = System.getProperty(PROP_ENDPOINT, DEFAULT_ENDPOINT);

        // Make sure we hit the SOAP endpoint (not necessarily what is stored in the WSDL)
        ((BindingProvider) p).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

        Client client = ClientProxy.getClient(p);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();

        AuthorizationPolicy auth = new AuthorizationPolicy();
        auth.setUserName(user);
        auth.setPassword(pass);
        auth.setAuthorizationType("Basic");
        conduit.setAuthorization(auth);

        HTTPClientPolicy http = new HTTPClientPolicy();
        http.setConnectionTimeout(5_000);
        http.setReceiveTimeout(15_000);
        http.setAllowChunking(false);
        conduit.setClient(http);
    }

    private StockQuoteDTO toDto(PublicStockQuote q) {
        if (q == null) return new StockQuoteDTO(null, null, null);

        // Map the WS object onto your DTO
        return new StockQuoteDTO(
                q.getSymbol(),
                q.getCompanyName(),
                q.getLastTradePrice(),
                null,                 // "change" not delivered by this WSDL -> keep null
                q.getStockExchange()
        );
    }

    private String requiredSystemProperty(String name) {
        String v = System.getProperty(name);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException(
                    "Missing required system property '" + name + "'. " +
                            "Set it on the WildFly JVM, e.g. -D" + name + "=..."
            );
        }
        return v.trim();
    }

    // ==================== BUY / SELL via Stock Exchange WS ====================

    /**
     * Executes a BUY order on the stock exchange via the TradingWebService.
     *
     * @param symbol Stock symbol (e.g., "AAPL")
     * @param shares Number of shares to buy
     * @return The execution price per share returned by the exchange
     * @throws RuntimeException if the WS call fails (triggers transaction rollback)
     */
    public java.math.BigDecimal buy(String symbol, int shares) {
        String sym = normalizeSymbol(symbol);
        if (sym == null || sym.isBlank()) {
            throw new IllegalArgumentException("Symbol must not be blank");
        }
        if (shares <= 0) {
            throw new IllegalArgumentException("Shares must be positive");
        }

        LOG.info("Executing BUY order: {} shares of {}", shares, sym);

        try {
            // Call the stock exchange WS to execute the buy order
            java.math.BigDecimal executionPrice = getPort().buy(sym, shares);

            if (executionPrice == null) {
                throw new RuntimeException("TradingService returned null price for BUY order");
            }

            LOG.info("BUY order executed: {} shares of {} at {}", shares, sym, executionPrice);
            return executionPrice;

        } catch (TradingWSException_Exception e) {
            LOG.error("TradingService BUY failed for symbol={}, shares={}: {}", sym, shares, e.getMessage());
            // Throw RuntimeException to trigger transaction rollback
            throw new RuntimeException("Stock exchange BUY order failed: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("TradingService BUY failed for symbol={}, shares={}", sym, shares, e);
            // Throw RuntimeException to trigger transaction rollback
            throw new RuntimeException("Stock exchange BUY order failed: " + e.getMessage(), e);
        }
    }

    /**
     * Executes a SELL order on the stock exchange via the TradingWebService.
     *
     * @param symbol Stock symbol (e.g., "AAPL")
     * @param shares Number of shares to sell
     * @return The execution price per share returned by the exchange
     * @throws RuntimeException if the WS call fails (triggers transaction rollback)
     */
    public java.math.BigDecimal sell(String symbol, int shares) {
        String sym = normalizeSymbol(symbol);
        if (sym == null || sym.isBlank()) {
            throw new IllegalArgumentException("Symbol must not be blank");
        }
        if (shares <= 0) {
            throw new IllegalArgumentException("Shares must be positive");
        }

        LOG.info("Executing SELL order: {} shares of {}", shares, sym);

        try {
            // Call the stock exchange WS to execute the sell order
            java.math.BigDecimal executionPrice = getPort().sell(sym, shares);

            if (executionPrice == null) {
                throw new RuntimeException("TradingService returned null price for SELL order");
            }

            LOG.info("SELL order executed: {} shares of {} at {}", shares, sym, executionPrice);
            return executionPrice;

        } catch (TradingWSException_Exception e) {
            LOG.error("TradingService SELL failed for symbol={}, shares={}: {}", sym, shares, e.getMessage());
            // Throw RuntimeException to trigger transaction rollback
            throw new RuntimeException("Stock exchange SELL order failed: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("TradingService SELL failed for symbol={}, shares={}", sym, shares, e);
            // Throw RuntimeException to trigger transaction rollback
            throw new RuntimeException("Stock exchange SELL order failed: " + e.getMessage(), e);
        }
    }
}
