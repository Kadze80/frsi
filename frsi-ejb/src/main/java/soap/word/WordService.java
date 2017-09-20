
package soap.word;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.8
 * Generated source version: 2.2
 *
 */
@WebServiceClient(name = "WordService", targetNamespace = "http://localhost:12525/WordToPdfConvert/WordToPdfConvertWS.asmx", wsdlLocation = "http://frsi.convertpdf.local:12525/WordToPdfConvert/WordToPdfConvertWS.asmx?WSDL")
public class WordService
        extends Service
{

    private final static URL WORDSERVICE_WSDL_LOCATION;
    private final static WebServiceException WORDSERVICE_EXCEPTION;
    private final static QName WORDSERVICE_QNAME = new QName("http://localhost:12525/WordToPdfConvert/WordToPdfConvertWS.asmx", "WordService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://frsi.convertpdf.local:12525/WordToPdfConvert/WordToPdfConvertWS.asmx?WSDL");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        WORDSERVICE_WSDL_LOCATION = url;
        WORDSERVICE_EXCEPTION = e;
    }

    public WordService() {
        super(__getWsdlLocation(), WORDSERVICE_QNAME);
    }

    public WordService(WebServiceFeature... features) {
        super(__getWsdlLocation(), WORDSERVICE_QNAME, features);
    }

    public WordService(URL wsdlLocation) {
        super(wsdlLocation, WORDSERVICE_QNAME);
    }

    public WordService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, WORDSERVICE_QNAME, features);
    }

    public WordService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public WordService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     *
     * @return
     *     returns WordServiceSoap
     */
    @WebEndpoint(name = "WordServiceSoap")
    public WordServiceSoap getWordServiceSoap() {
        return super.getPort(new QName("http://localhost:12525/WordToPdfConvert/WordToPdfConvertWS.asmx", "WordServiceSoap"), WordServiceSoap.class);
    }

    /**
     *
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns WordServiceSoap
     */
    @WebEndpoint(name = "WordServiceSoap")
    public WordServiceSoap getWordServiceSoap(WebServiceFeature... features) {
        return super.getPort(new QName("http://localhost:12525/WordToPdfConvert/WordToPdfConvertWS.asmx", "WordServiceSoap"), WordServiceSoap.class, features);
    }

    private static URL __getWsdlLocation() {
        if (WORDSERVICE_EXCEPTION!= null) {
            throw WORDSERVICE_EXCEPTION;
        }
        return WORDSERVICE_WSDL_LOCATION;
    }

}
