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

    protected String marshallObject(Object obj, boolean doJSON) {
        // boolean doJSON = headers.getMediaType().isCompatible(MediaType.APPLICATION_JSON);
        boolean doFancy = false;
        try {
            doFancy = Boolean.parseBoolean(url.getQueryParameters(true).getFirst("doFancy"));
        } catch (Exception e) {
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
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public String getDataXML(@DefaultValue("???") @QueryParam("field") String field) {
        Data d = new Data(field);

        return marshallObject(d, false);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getDataJSON(@DefaultValue("???") @QueryParam("field") String field) {
        Data d = new Data(field);

        return marshallObject(d, true);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getDataPlain(@DefaultValue("???") @QueryParam("field") String field) {
        Data d = new Data(field);

        return d.toString();
    }
}
