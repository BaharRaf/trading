
package net.froihofer.dsfinance.ws.trading.api;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.froihofer.dsfinance.ws.trading.api package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Buy_QNAME = new QName("http://api.trading.ws.dsfinance.froihofer.net/", "buy");
    private final static QName _BuyResponse_QNAME = new QName("http://api.trading.ws.dsfinance.froihofer.net/", "buyResponse");
    private final static QName _FindStockQuotesByCompanyName_QNAME = new QName("http://api.trading.ws.dsfinance.froihofer.net/", "findStockQuotesByCompanyName");
    private final static QName _FindStockQuotesByCompanyNameResponse_QNAME = new QName("http://api.trading.ws.dsfinance.froihofer.net/", "findStockQuotesByCompanyNameResponse");
    private final static QName _GetStockQuoteHistory_QNAME = new QName("http://api.trading.ws.dsfinance.froihofer.net/", "getStockQuoteHistory");
    private final static QName _GetStockQuoteHistoryResponse_QNAME = new QName("http://api.trading.ws.dsfinance.froihofer.net/", "getStockQuoteHistoryResponse");
    private final static QName _GetStockQuotes_QNAME = new QName("http://api.trading.ws.dsfinance.froihofer.net/", "getStockQuotes");
    private final static QName _GetStockQuotesResponse_QNAME = new QName("http://api.trading.ws.dsfinance.froihofer.net/", "getStockQuotesResponse");
    private final static QName _Sell_QNAME = new QName("http://api.trading.ws.dsfinance.froihofer.net/", "sell");
    private final static QName _SellResponse_QNAME = new QName("http://api.trading.ws.dsfinance.froihofer.net/", "sellResponse");
    private final static QName _TradingWSException_QNAME = new QName("http://api.trading.ws.dsfinance.froihofer.net/", "TradingWSException");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.froihofer.dsfinance.ws.trading.api
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Buy }
     * 
     */
    public Buy createBuy() {
        return new Buy();
    }

    /**
     * Create an instance of {@link BuyResponse }
     * 
     */
    public BuyResponse createBuyResponse() {
        return new BuyResponse();
    }

    /**
     * Create an instance of {@link FindStockQuotesByCompanyName }
     * 
     */
    public FindStockQuotesByCompanyName createFindStockQuotesByCompanyName() {
        return new FindStockQuotesByCompanyName();
    }

    /**
     * Create an instance of {@link FindStockQuotesByCompanyNameResponse }
     * 
     */
    public FindStockQuotesByCompanyNameResponse createFindStockQuotesByCompanyNameResponse() {
        return new FindStockQuotesByCompanyNameResponse();
    }

    /**
     * Create an instance of {@link GetStockQuoteHistory }
     * 
     */
    public GetStockQuoteHistory createGetStockQuoteHistory() {
        return new GetStockQuoteHistory();
    }

    /**
     * Create an instance of {@link GetStockQuoteHistoryResponse }
     * 
     */
    public GetStockQuoteHistoryResponse createGetStockQuoteHistoryResponse() {
        return new GetStockQuoteHistoryResponse();
    }

    /**
     * Create an instance of {@link GetStockQuotes }
     * 
     */
    public GetStockQuotes createGetStockQuotes() {
        return new GetStockQuotes();
    }

    /**
     * Create an instance of {@link GetStockQuotesResponse }
     * 
     */
    public GetStockQuotesResponse createGetStockQuotesResponse() {
        return new GetStockQuotesResponse();
    }

    /**
     * Create an instance of {@link Sell }
     * 
     */
    public Sell createSell() {
        return new Sell();
    }

    /**
     * Create an instance of {@link SellResponse }
     * 
     */
    public SellResponse createSellResponse() {
        return new SellResponse();
    }

    /**
     * Create an instance of {@link TradingWSException }
     * 
     */
    public TradingWSException createTradingWSException() {
        return new TradingWSException();
    }

    /**
     * Create an instance of {@link PublicStockQuote }
     * 
     */
    public PublicStockQuote createPublicStockQuote() {
        return new PublicStockQuote();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Buy }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Buy }{@code >}
     */
    @XmlElementDecl(namespace = "http://api.trading.ws.dsfinance.froihofer.net/", name = "buy")
    public JAXBElement<Buy> createBuy(Buy value) {
        return new JAXBElement<Buy>(_Buy_QNAME, Buy.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BuyResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link BuyResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://api.trading.ws.dsfinance.froihofer.net/", name = "buyResponse")
    public JAXBElement<BuyResponse> createBuyResponse(BuyResponse value) {
        return new JAXBElement<BuyResponse>(_BuyResponse_QNAME, BuyResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindStockQuotesByCompanyName }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link FindStockQuotesByCompanyName }{@code >}
     */
    @XmlElementDecl(namespace = "http://api.trading.ws.dsfinance.froihofer.net/", name = "findStockQuotesByCompanyName")
    public JAXBElement<FindStockQuotesByCompanyName> createFindStockQuotesByCompanyName(FindStockQuotesByCompanyName value) {
        return new JAXBElement<FindStockQuotesByCompanyName>(_FindStockQuotesByCompanyName_QNAME, FindStockQuotesByCompanyName.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FindStockQuotesByCompanyNameResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link FindStockQuotesByCompanyNameResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://api.trading.ws.dsfinance.froihofer.net/", name = "findStockQuotesByCompanyNameResponse")
    public JAXBElement<FindStockQuotesByCompanyNameResponse> createFindStockQuotesByCompanyNameResponse(FindStockQuotesByCompanyNameResponse value) {
        return new JAXBElement<FindStockQuotesByCompanyNameResponse>(_FindStockQuotesByCompanyNameResponse_QNAME, FindStockQuotesByCompanyNameResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStockQuoteHistory }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link GetStockQuoteHistory }{@code >}
     */
    @XmlElementDecl(namespace = "http://api.trading.ws.dsfinance.froihofer.net/", name = "getStockQuoteHistory")
    public JAXBElement<GetStockQuoteHistory> createGetStockQuoteHistory(GetStockQuoteHistory value) {
        return new JAXBElement<GetStockQuoteHistory>(_GetStockQuoteHistory_QNAME, GetStockQuoteHistory.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStockQuoteHistoryResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link GetStockQuoteHistoryResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://api.trading.ws.dsfinance.froihofer.net/", name = "getStockQuoteHistoryResponse")
    public JAXBElement<GetStockQuoteHistoryResponse> createGetStockQuoteHistoryResponse(GetStockQuoteHistoryResponse value) {
        return new JAXBElement<GetStockQuoteHistoryResponse>(_GetStockQuoteHistoryResponse_QNAME, GetStockQuoteHistoryResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStockQuotes }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link GetStockQuotes }{@code >}
     */
    @XmlElementDecl(namespace = "http://api.trading.ws.dsfinance.froihofer.net/", name = "getStockQuotes")
    public JAXBElement<GetStockQuotes> createGetStockQuotes(GetStockQuotes value) {
        return new JAXBElement<GetStockQuotes>(_GetStockQuotes_QNAME, GetStockQuotes.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStockQuotesResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link GetStockQuotesResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://api.trading.ws.dsfinance.froihofer.net/", name = "getStockQuotesResponse")
    public JAXBElement<GetStockQuotesResponse> createGetStockQuotesResponse(GetStockQuotesResponse value) {
        return new JAXBElement<GetStockQuotesResponse>(_GetStockQuotesResponse_QNAME, GetStockQuotesResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Sell }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link Sell }{@code >}
     */
    @XmlElementDecl(namespace = "http://api.trading.ws.dsfinance.froihofer.net/", name = "sell")
    public JAXBElement<Sell> createSell(Sell value) {
        return new JAXBElement<Sell>(_Sell_QNAME, Sell.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SellResponse }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link SellResponse }{@code >}
     */
    @XmlElementDecl(namespace = "http://api.trading.ws.dsfinance.froihofer.net/", name = "sellResponse")
    public JAXBElement<SellResponse> createSellResponse(SellResponse value) {
        return new JAXBElement<SellResponse>(_SellResponse_QNAME, SellResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TradingWSException }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link TradingWSException }{@code >}
     */
    @XmlElementDecl(namespace = "http://api.trading.ws.dsfinance.froihofer.net/", name = "TradingWSException")
    public JAXBElement<TradingWSException> createTradingWSException(TradingWSException value) {
        return new JAXBElement<TradingWSException>(_TradingWSException_QNAME, TradingWSException.class, null, value);
    }

}
