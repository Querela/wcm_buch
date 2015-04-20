package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Erik on 15.04.2015.
 */
public class Utils {
    private final static Logger log = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    public final static String WILDCARD_URI = "/*";
    public final static String API_URI = "/api/";
    public final static String STATIC_FILES_URI = "/static/";
    public final static String STATIC_HTML_URI = "/html/";
    public final static String STATIC_IMAGES_URI = "/images/";
    public final static String STATIC_FONTS_URI = "/fonts/";
    public final static String STATIC_SCRIPTS_URI = "/js/";
    public final static String STATIC_STYLES_URI = "/css/";

    public final static String WILDCARD_SERVLET_MAPPING = WILDCARD_URI;
    public final static String API_URI_SERVLET_MAPPING = API_URI + "*";
    public final static String STATIC_FILES_URI_SERVLET_MAPPING = STATIC_FILES_URI + "*";

    // TODO: use correct file, not testing thingy ...
    public final static String STATIC_FILE_SPA_INDEX_URI = STATIC_FILES_URI + "readme.txt";

    // -------------------------------------------------------------------------

    /**
     * Converts an object <i>obj</i> into an XML-String (or JSON if <i>doJSON</i> is <i>true</i>).
     *
     * @param obj     Object to convert (marshall)
     * @param doJSON  if <i>true</i> then we convert to JSON instead of XML
     * @param doFancy if <i>true</i> then the string will be formatted (indented etc.) else in a single line
     * @return String (XML / JSON)
     */
    public static String marshallObject(Object obj, boolean doJSON, boolean doFancy) {
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller m = context.createMarshaller();
            if (doJSON) {
                m.setProperty("eclipselink.media-type", "application/json");
            }
            if (doFancy) {
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            }
            StringWriter sw = new StringWriter();
            m.marshal(obj, sw);

            return sw.toString();
        } catch (JAXBException e) {
            // log.error("marshalling from obj to xml", e);
            String try2 = marshallObjectJAXB(obj, doJSON, doFancy);
            return (try2 != null) ? try2 : serializeError(e);
        }
    }

    protected static String marshallObjectJAXB(Object obj, boolean doJSON, boolean doFancy) {
        try {

            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller m = context.createMarshaller();
            if (doJSON) {
                m.setProperty("eclipselink.media-type", "application/json");
            }
            if (doFancy) {
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            }
            StringWriter sw = new StringWriter();

            QName qName = new QName(obj.getClass().getPackage().getName(), obj.getClass().getSimpleName());
            JAXBElement<Object> root = new JAXBElement(qName, obj.getClass(), obj);
            m.marshal(root, sw);

            return sw.toString();
        } catch (JAXBException e) {
            // log.error("marshalling from obj to xml", e);
            return null;
        }
    }

    /**
     * Convert an Exception/Throwable into a String ...
     *
     * @param t Exception
     * @return String
     */
    protected static String serializeError(Throwable t) {
        if (t == null) {
            return "no error ...";
        } else {
            log.error("...", t);

            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        }
    }
}
