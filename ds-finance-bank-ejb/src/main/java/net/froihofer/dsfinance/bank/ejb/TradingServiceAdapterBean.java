package net.froihofer.dsfinance.bank.ejb;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import net.froihofer.dsfinance.bank.dto.StockQuoteDTO;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TradingService adapter. For Part 2 we intentionally use a dynamic SOAP client so that
 * no generated stubs are required. This demonstrates the Web Service call.
 *
 * Configure credentials via system properties:
 * -Dtrading.ws.user=... -Dtrading.ws.pass=...
 */
@Stateless
@RolesAllowed({"employee","customer"})
public class TradingServiceAdapterBean {

  private static final Logger log = LoggerFactory.getLogger(TradingServiceAdapterBean.class);

  // WSDL as given in the assignment:
  private static final String WSDL_URL = "https://edu.dedisys.org/ds-finance/ws/TradingService?wsdl";

  public List<StockQuoteDTO> findStockQuotesByCompanyName(String companyNameQuery) {
    try {
      JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
      Client client = dcf.createClient(new URL(WSDL_URL));

      // Basic authentication (required by the course WS):
      String user = System.getProperty("trading.ws.user", "csdc26vz_04");
      String pass = System.getProperty("trading.ws.pass", "Eequaizah4sh");
      HTTPConduit conduit = (HTTPConduit) client.getConduit();
      AuthorizationPolicy auth = new AuthorizationPolicy();
      auth.setUserName(user);
      auth.setPassword(pass);
      auth.setAuthorizationType("Basic");
      conduit.setAuthorization(auth);

      // Operation name according to WSDL documentation snippet:
      Object[] response = client.invoke("findStockQuotesByCompanyName", companyNameQuery);

      // Convert response to DTOs using reflection (works with generated types too):
      return mapQuotes(response.length > 0 ? response[0] : null);
    } catch (Exception e) {
      log.error("TradingService call failed", e);
      throw new RuntimeException("TradingService call failed: " + e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  private List<StockQuoteDTO> mapQuotes(Object raw) {
    List<StockQuoteDTO> out = new ArrayList<>();
    if (raw == null) return out;

    if (raw instanceof List<?>) {
      for (Object o : (List<?>) raw) out.add(toDto(o));
      return out;
    }
    if (raw.getClass().isArray()) {
      int n = java.lang.reflect.Array.getLength(raw);
      for (int i = 0; i < n; i++) out.add(toDto(java.lang.reflect.Array.get(raw, i)));
      return out;
    }

    // Single item:
    out.add(toDto(raw));
    return out;
  }

  private StockQuoteDTO toDto(Object quote) {
    if (quote == null) return new StockQuoteDTO(null, null, null, Instant.now());
    try {
      String symbol = (String) invokeGetter(quote, "getSymbol");
      String company = (String) invokeGetter(quote, "getCompanyName");
      Object priceObj = invokeGetter(quote, "getPrice");
      BigDecimal price = priceObj instanceof BigDecimal ? (BigDecimal) priceObj : (priceObj != null ? new BigDecimal(priceObj.toString()) : null);
      Object tsObj = tryInvokeGetter(quote, "getTimestamp");
      Instant ts = (tsObj instanceof Instant) ? (Instant) tsObj : Instant.now();
      return new StockQuoteDTO(symbol, company, price, ts);
    } catch (Exception e) {
      // Fallback: return minimal string form
      return new StockQuoteDTO(null, quote.toString(), null, Instant.now());
    }
  }

  private Object invokeGetter(Object target, String methodName) throws Exception {
    Method m = target.getClass().getMethod(methodName);
    return m.invoke(target);
  }

  private Object tryInvokeGetter(Object target, String methodName) {
    try {
      return invokeGetter(target, methodName);
    } catch (Exception e) {
      return null;
    }
  }
}
