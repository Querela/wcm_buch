package de.uni_leipzig.wcmprak.books.wcmbookserver.serve.resources;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.GoodreadsAPIAdapter;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.GoodreadsAPIResponseParser;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.GoodreadsScreenScraper;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.AuthorInfo;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.Book;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.BookEditionsList;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.SeriesInfo;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Props;
import de.uni_leipzig.wcmprak.books.wcmbookserver.serve.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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

    private final static String GOODREADS_BASE_URL = "https://www.goodreads.com/";

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
    public String getBookResultListForSearchTerm(@PathParam("searchString") String searchString) {
        return marshallObjectByMediaType((new Object() {
        }.getClass().getEnclosingMethod().getName() + ": ") + searchString);
    }

    @GET
    @Path("/book/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getBookByGoodreadsID(@PathParam("id") String bookID) {
        GoodreadsAPIAdapter api = new GoodreadsAPIAdapter();
        GoodreadsAPIResponseParser parser = new GoodreadsAPIResponseParser();
        GoodreadsScreenScraper sscraper = new GoodreadsScreenScraper();

        Props props = new Props();
        props.setStringProp("goodreads.api.key", "RwUzZwkv94PCodD1lMF5g");

        api.configureWith(props);
        parser.configureWith(props);
        sscraper.configureWith(props);

        try {
            api.initialize();
            parser.initialize();
            sscraper.initialize();
        } catch (Exception e) {
            log.error("init goodreads", e);
        }

        String bookContent = api.getDataForBookID(bookID);
        Book book = parser.parseBookData(bookContent, GOODREADS_BASE_URL); // TODO: url?

        return marshallObjectByMediaType(book);
    }

    @GET
    @Path("/book/{id}/editions")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getBookEditionsByBookGoodreadsID(@PathParam("id") String bookID) {
        GoodreadsScreenScraper gss = new GoodreadsScreenScraper();
        gss.configureWith(null);
        try {
            gss.initialize();
        } catch (Exception e) {
            log.error("goodreads init", e);
        }

        BookEditionsList bookEditionsList = gss.parseEditionsPage(4640799);

        return marshallObjectByMediaType(bookEditionsList);
    }

    @GET
    @Path("/book/{id}/languages")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getBookLanguagesByBookGoodreadsID(@PathParam("id") String bookID) {
        return marshallObjectByMediaType((new Object() {
        }.getClass().getEnclosingMethod().getName() + ": ") + bookID);
    }

    @GET
    @Path("/book/{id}/series")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getBooksSeriesByBookGoodreadsID(@PathParam("id") String bookID) {
        return marshallObjectByMediaType((new Object() {
        }.getClass().getEnclosingMethod().getName() + ": ") + bookID);
    }

    @GET
    @Path("/series/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getSeriesByGoodreadsID(@PathParam("id") String seriesID) {
        GoodreadsAPIAdapter api = new GoodreadsAPIAdapter();
        GoodreadsAPIResponseParser parser = new GoodreadsAPIResponseParser();

        Props props = new Props();
        props.setStringProp("goodreads.api.key", "RwUzZwkv94PCodD1lMF5g");

        api.configureWith(props);
        parser.configureWith(props);

        try {
            api.initialize();
            parser.initialize();
        } catch (Exception e) {
            log.error("init goodreads", e);
        }

        String seriesContent = api.getDataForSeriesID(seriesID);
        SeriesInfo series = parser.parseSeriesData(seriesContent, GOODREADS_BASE_URL);

        return marshallObjectByMediaType(series);
    }

    @GET
    @Path("/author/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getAuthorByGoodreadsID(@PathParam("id") String authorID) {
        GoodreadsAPIAdapter api = new GoodreadsAPIAdapter();
        GoodreadsAPIResponseParser parser = new GoodreadsAPIResponseParser();

        Props props = new Props();
        props.setStringProp("goodreads.api.key", "RwUzZwkv94PCodD1lMF5g");

        api.configureWith(props);
        parser.configureWith(props);

        try {
            api.initialize();
            parser.initialize();
        } catch (Exception e) {
            log.error("init goodreads", e);
        }

        String[] authorsBooks = api.getAllPagesDataForAuthorID(authorID);
        String[] urls = new String[authorsBooks.length];
        for (int i = 0; i < authorsBooks.length; i++) urls[i] = GOODREADS_BASE_URL;
        AuthorInfo authorInfo = parser.parseAuthorsBookData(authorsBooks, urls);

        return marshallObjectByMediaType(authorInfo);
    }

    @GET
    @Path("/author/{id}/books")
    @Produces({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    public String getAuthorBooksByAuthorGoodreadsID(@PathParam("id") String authorID) {
        return marshallObjectByMediaType((new Object() {
        }.getClass().getEnclosingMethod().getName() + ": ") + authorID);
    }
}
