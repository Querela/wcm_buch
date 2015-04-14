package de.uni_leipzig.wcmprak.books.wcmbookserver.serve.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Erik on 14.04.2015.
 */
@Path("/")
public class RequestHandler {
    private final static Logger log = LoggerFactory.getLogger(RequestHandler.class);


    @Context
    UriInfo url;

    @Context
    Request request;

    @Context
    HttpHeaders headers;

    public RequestHandler() {
    }

    /**
     * Converts an object <i>obj</i> into an XML-String (or JSON if <i>doJSON</i> is <i>true</i>).
     *
     * @param obj    Object to convert (marshall)
     * @param doJSON if <i>true</i> then we convert to JSON instead of XML
     * @return String (XML / JSON)
     */
    protected String marshallObject(Object obj, boolean doJSON) {
        // boolean doJSON = headers.getMediaType().isCompatible(MediaType.APPLICATION_JSON);
        boolean doFancy = false;
        try {
            doFancy = Boolean.parseBoolean(url.getQueryParameters(true).getFirst("doFancy"));
        } catch (Exception ignored) {
        }


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
            log.error("marshalling from obj to xml", e);
            return serializeError(e);
        }
    }

    /**
     * Converts an object <i>obj</i> into String-form. Uses the <i>ACCEPT</i>-HTTP-Header to determine if XML or JSON.
     *
     * @param obj Object to serialize/convert/marshall
     * @return String (XML / JSON depending on Content-Encoding (?))
     * @see {@link #marshallObject}
     */
    protected String marshallObjectByMediaType(Object obj) {
        boolean doJSON = true;
        try {
            doJSON = headers.getMediaType().isCompatible(MediaType.APPLICATION_JSON_TYPE);
        } catch (Exception ignored) {
        }

        return marshallObject(obj, doJSON);
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
            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        }
    }

    @GET
    @Path("/search/{searchString}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public String getBookResultListForSearchTermXML(@PathParam("searchString") String searchString) {
        return (new Object() {
        }.getClass().getEnclosingMethod().getName() + ": ") + searchString;
    }

    @GET
    @Path("/book/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public String getBookByGoodreadsIDXML(@PathParam("id") String bookID) {
        return (new Object() {
        }.getClass().getEnclosingMethod().getName() + ": ") + bookID;
    }

    @GET
    @Path("/book/{id}/editions")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public String getBookEditionsByBookGoodreadsIDXML(@PathParam("id") String bookID) {
        return (new Object() {
        }.getClass().getEnclosingMethod().getName() + ": ") + bookID;
    }

    @GET
    @Path("/author/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public String getAuthorByGoodreadsIDXML(@PathParam("id") String authorID) {
        return (new Object() {
        }.getClass().getEnclosingMethod().getName() + ": ") + authorID;
    }

    @GET
    @Path("/author/{id}/books")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public String getAuthorBooksByAuthorGoodreadsIDXML(@PathParam("id") String authorID) {
        return (new Object() {
        }.getClass().getEnclosingMethod().getName() + ": ") + authorID;
    }
}
