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

@Stateless
@RolesAllowed({"employee", "customer"})
public class TradingServiceAdapterBean {

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
            return out;

        } catch (TradingWSException_Exception e) {
            LOG.warn("TradingService returned a domain error for query='{}': {}", q, e.getMessage());
            throw new RuntimeException("TradingService call failed: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("TradingService call failed for query='{}'", q, e);
            throw new RuntimeException("TradingService call failed: " + e.getMessage(), e);
        }
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

    private static String requiredSystemProperty(String name) {
        String v = System.getProperty(name);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException(
                    "Missing required system property '" + name + "'. " +
                            "Set it on the WildFly JVM, e.g. -D" + name + "=..."
            );
        }
        return v.trim();
    }
}
