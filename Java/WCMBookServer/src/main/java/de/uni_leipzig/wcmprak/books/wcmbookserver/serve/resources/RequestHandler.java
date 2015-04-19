package de.uni_leipzig.wcmprak.books.wcmbookserver.serve.resources;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.*;
import de.uni_leipzig.wcmprak.books.wcmbookserver.serve.DataExtractor;
import de.uni_leipzig.wcmprak.books.wcmbookserver.serve.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

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
     * Converts an object <i>obj</i> into String-form. Uses the <i>ACCEPT</i>-HTTP-Header to determine if XML or JSON.
     *
     * @param obj Object to serialize/convert/marshall
     * @return String (XML / JSON depending on Content-Encoding (?))
     * @see {@link de.uni_leipzig.wcmprak.books.wcmbookserver.serve.Utils#marshallObject(Object, boolean, boolean)}
     */
    protected String marshallObjectByMediaType(Object obj) {
        boolean doFancy = false;
        try {
            doFancy = Boolean.parseBoolean(url.getQueryParameters(true).getFirst("doFancy"));
        } catch (Exception ignored) {
        }

        boolean doJSON = true;
        try {
            boolean isXML = false;
            isXML |= headers.getAcceptableMediaTypes().contains(MediaType.APPLICATION_XML_TYPE);
            isXML |= headers.getAcceptableMediaTypes().contains(MediaType.TEXT_XML_TYPE);

            boolean isJSON = false;
            isJSON |= headers.getAcceptableMediaTypes().contains(MediaType.APPLICATION_JSON_TYPE);

            // if not xml output than we want json ...? -> no third option :'(
            doJSON = isJSON || !isXML;
        } catch (Exception ignored) {
        }

        return Utils.marshallObject(obj, doJSON, doFancy);
    }

    @GET
    @Path("/search/{searchString}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getBookResultListForSearchTerm(@PathParam("searchString") String searchString, @DefaultValue("1") @QueryParam("page") int page) {
        SearchResultList searchResultList = DataExtractor.getInstance().getSearchResults(searchString, page);

        return marshallObjectByMediaType(searchResultList);
    }

    @GET
    @Path("/search/{searchString}/{page}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getBookResultListForSearchTerm2(@PathParam("searchString") String searchString, @PathParam("page") int page) {
        SearchResultList searchResultList = DataExtractor.getInstance().getSearchResults(searchString, page);

        return marshallObjectByMediaType(searchResultList);
    }

    @GET
    @Path("/book/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getBookByGoodreadsID(@PathParam("id") String bookID) {
        Book book = DataExtractor.getInstance().getBook(bookID);

        return marshallObjectByMediaType(book);
    }

    @GET
    @Path("/book/{id}/editions")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getBookEditionsByBookGoodreadsID(@PathParam("id") String bookID) {
        Book book = DataExtractor.getInstance().getBook(bookID);

        BookEditionsList bookEditionsList = DataExtractor.getInstance().getEditions("" + book.getGoodreadsEditionsID());

        return marshallObjectByMediaType(bookEditionsList);
    }

    @GET
    @Path("/book/editions/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getBookEditionsByEditionsGoodreadsID(@PathParam("id") String editionsID) {
        BookEditionsList bookEditionsList = DataExtractor.getInstance().getEditions(editionsID);

        return marshallObjectByMediaType(bookEditionsList);
    }

    @GET
    @Path("/book/{id}/languages")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getBookLanguagesByBookGoodreadsID(@PathParam("id") String bookID) {
        Book book = DataExtractor.getInstance().getBook(bookID);

        MapLanguageBookInfo mapLanguageBookInfo = DataExtractor.getInstance().getLanguages("" + book.getGoodreadsEditionsID());

        return marshallObjectByMediaType(mapLanguageBookInfo);
    }

    @GET
    @Path("/book/languages/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getBookLanguagesByEditionsGoodreadsID(@PathParam("id") String editionsID) {
        MapLanguageBookInfo mapLanguageBookInfo = DataExtractor.getInstance().getLanguages(editionsID);

        return marshallObjectByMediaType(mapLanguageBookInfo);
    }

    @GET
    @Path("/book/{id}/series")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getBooksSeriesByBookGoodreadsID(@PathParam("id") String bookID) {
        Book book = DataExtractor.getInstance().getBook(bookID);

        SeriesInfo series = DataExtractor.getInstance().getSeries("" + book.getSeries().getGoodreadsID());

        return marshallObjectByMediaType(series);
    }

    @GET
    @Path("/series/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getSeriesByGoodreadsID(@PathParam("id") String seriesID) {
        SeriesInfo series = DataExtractor.getInstance().getSeries(seriesID);

        return marshallObjectByMediaType(series);
    }

    @GET
    @Path("/author/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getAuthorByGoodreadsID(@PathParam("id") String authorID) {
        AuthorInfo authorInfo = DataExtractor.getInstance().getAuthorInfo(authorID);

        return marshallObjectByMediaType(authorInfo);
    }

    @GET
    @Path("/author/{id}/books")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getAuthorBooksByAuthorGoodreadsID(@PathParam("id") String authorID) {
        // TODO: subset of: "/author/{id}"
        return marshallObjectByMediaType((new Object() {
        }.getClass().getEnclosingMethod().getName() + ": ") + authorID);
    }
}
