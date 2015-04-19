package de.uni_leipzig.wcmprak.books.wcmbookserver.extract;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Configurable;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Initializable;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.JSoup;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Props;
import org.glassfish.jersey.filter.LoggingFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Properties;

/**
 * Adapter-class to wrap all query calls in simple methods.
 * <p/>
 * Created by Erik on 01.12.2014.
 */
public class GoodreadsAPIAdapter implements Configurable, Initializable {
    private final static Logger log = LoggerFactory.getLogger(GoodreadsAPIAdapter.class);

    private final static String BASE_URI = "https://www.goodreads.com/";
    private final static String BOOK_URI = BASE_URI + "book/show";
    private final static String AUTHORS_BOOKS_URI = BASE_URI + "author/list";
    private final static String SERIES_BOOKS_URI = BASE_URI + "series/show.xml";
    private final static String SEARCH_BOOKS_URI = BASE_URI + "search/index.xml";

    private String API_KEY = null;

    private boolean hasBeenInitialized = false;
    private Props props = null;
    private Client client = null;
    private boolean logRequests = false;

    /**
     * Returns the String response for a book metadata query for the given <i>bookID</i>.
     *
     * @param bookID ID of book to query
     * @return String with XML-Document or null on error
     */
    public String getDataForBookID(String bookID) {
        log.debug("Request content for {} ...", bookID);
        Response response = getWebTargetForBookID(bookID).request().buildGet().invoke();

        // Check if correct response type (application/xml)
        if (isValidXMLResponseType(response)) {
            log.debug("Read {} bytes ...", response.getLength());
            return response.readEntity(String.class);
        } else {
            return null;
        } // if-else
    }

    /**
     * Returns the String response for a author metadata query for the given <i>authorID</i> and <i>page</i>.
     *
     * @param authorID ID of author to query
     * @param page     Page of result list
     * @return String with XML-Document or null on error
     */
    public String getDataForAuthorID(String authorID, int page) {
        log.debug("Request content for {} (page {}) ...", authorID, page);
        Response response = getWebTargetForAuthorID(authorID, page).request().buildGet().invoke();

        // Check if correct response type (application/xml)
        if (isValidXMLResponseType(response)) {
            log.debug("Read {} bytes ...", response.getLength());
            return response.readEntity(String.class);
        } else {
            return null;
        } // if-else
    }

    /**
     * Returns the String response for a search metadata query for the given <i>searchTerm</i> and <i>page</i>.
     *
     * @param searchTerm search term
     * @param page       Page of result list
     * @return String with XML-Document or null on error
     */
    public String getDataForSearchTerm(String searchTerm, int page) {
        log.debug("Request content for search: {} (page {}) ...", searchTerm, page);
        Response response = getWebTargetForSearchTerm(searchTerm, page).request().buildGet().invoke();

        // Check if correct response type (application/xml)
        if (isValidXMLResponseType(response)) {
            log.debug("Read {} bytes ...", response.getLength());
            return response.readEntity(String.class);
        } else {
            return null;
        } // if-else
    }

    /**
     * Returns the String response for a author metadata query for the given <i>authorID</i> and first page.
     *
     * @param authorID ID of author to query
     * @return String with XML-Document or null on error
     */
    public String getFirstPageDataForAuthorID(String authorID) {
        return getDataForAuthorID(authorID, 1);
    }

    /**
     * Returns the String response for a search metadata query for the given <i>searchTerm</i> and first page.
     *
     * @param searchTerm search term to query
     * @return String with XML-Document or null on error
     */
    public String getFirstPageDataForSearchTerm(String searchTerm) {
        return getDataForSearchTerm(searchTerm, 1);
    }

    /**
     * Returns the String responses for a author metadata query for the given <i>authorID</i> and all pages.
     *
     * @param authorID ID of author to query
     * @return String[] with each entry either a XML-Document or null on error
     */
    public String[] getAllPagesDataForAuthorID(String authorID) {
        log.debug("Request content for {} (all pages) ...", authorID);
        String firstPage = getDataForAuthorID(authorID, 1);
        if (firstPage == null) {
            return new String[0];
        } // if

        Element paginateEle = Jsoup.parse(firstPage, AUTHORS_BOOKS_URI + '/' + authorID, Parser.xmlParser()).select("GoodreadsResponse > author > books").first();
        int start = Integer.parseInt(JSoup.getAttributeValue(paginateEle, "start"));
        int end = Integer.parseInt(JSoup.getAttributeValue(paginateEle, "end"));
        int total = Integer.parseInt(JSoup.getAttributeValue(paginateEle, "total"));
        log.debug("First result page: Results {} - {} of {}", start, end, total);
        int resultsPerPage = end - start + 1;
        int countPages = ((total + resultsPerPage - 1) / resultsPerPage);
        log.debug("Total pages to request: {}, pages left: {}", countPages, countPages - 1);

        String[] result = new String[countPages];
        result[0] = firstPage;

        for (int pageNr = 2; pageNr <= countPages; pageNr++) {
            result[pageNr - 1] = getDataForAuthorID(authorID, pageNr);
            // TODO: check null pages? -> List.toArray()?
        } // for

        return result;
    }

    /**
     * Returns the String responses for a search metadata query for the given <i>searchTerm</i> and all pages.
     *
     * @param searchTerm search term to query
     * @return String[] with each entry either a XML-Document or null on error
     */
    public String[] getAllPagesDataForSearchTerm(String searchTerm) {
        log.debug("Request content for search term: {} (all pages) ...", searchTerm);
        String firstPage = getDataForSearchTerm(searchTerm, 1);
        if (firstPage == null) {
            return new String[0];
        } // if

        Element paginateEle = Jsoup.parse(firstPage, SEARCH_BOOKS_URI, Parser.xmlParser()).select("GoodreadsResponse > search").first();
        int start = Integer.parseInt(JSoup.getElementValue(paginateEle.select("results-start")));
        int end = Integer.parseInt(JSoup.getElementValue(paginateEle.select("results-end")));
        int total = Integer.parseInt(JSoup.getElementValue(paginateEle.select("total-results")));
        log.debug("First result page: Results {} - {} of {}", start, end, total);
        int resultsPerPage = end - start + 1;
        int countPages = ((total + resultsPerPage - 1) / resultsPerPage);
        log.debug("Total pages to request: {}, pages left: {}", countPages, countPages - 1);

        String[] result = new String[countPages];
        result[0] = firstPage;

        for (int pageNr = 2; pageNr <= countPages; pageNr++) {
            result[pageNr - 1] = getDataForSearchTerm(searchTerm, pageNr);
            // TODO: check null pages? -> List.toArray()?
        } // for

        return result;
    }

    /**
     * Returns the String response for a series metadata query for the given <i>seriesID</i>.
     *
     * @param seriesID ID of series to query
     * @return String with XML-Document or null on error
     */
    public String getDataForSeriesID(String seriesID) {
        log.debug("Request content for {} ...", seriesID);
        Response response = getWebTargetForSeriesID(seriesID).request().buildGet().invoke();

        // Check if correct response type (application/xml)
        if (isValidXMLResponseType(response)) {
            log.debug("Read {} bytes ...", response.getLength());
            return fixSeriesData(response.readEntity(String.class));
        } else {
            return null;
        } // if-else
    }

    /**
     * Fixes the returned XML of a series request. There a parts url encoded!
     *
     * @param seriesContent Text to decode parts of
     * @return Decoded String
     */
    protected String fixSeriesData(String seriesContent) {
        if (seriesContent == null) {
            return null;
        } // if

        // Check if String has to be decoded!
        int start = seriesContent.indexOf("&lt;work");
        if (start == -1) {
            // No decoding neccessary
            return seriesContent;
        } // if

        StringBuilder sb = new StringBuilder(seriesContent);

        int stop = 0;
        int l = "/work&gt;".length();

        // Decode each <work/> fragment
        while (((start = sb.indexOf("&lt;work", start)) != -1) && ((stop = sb.indexOf("/work&gt;", start)) != -1)) {
            for (int pos = stop + l; pos >= start; ) {
                pos = sb.lastIndexOf("&quot;", pos);
                if (pos < start) {
                    break;
                } // if
                sb.replace(pos, pos + 6, "\"");
            } // for

            stop = sb.lastIndexOf("/work&gt;", stop);

            for (int pos = stop + l; pos >= start; ) {
                pos = sb.lastIndexOf("&lt;", pos);
                if (pos < start) {
                    break;
                } // if
                sb.replace(pos, pos + 4, "<");
            } // for

            stop = sb.lastIndexOf("/work&gt;", stop);

            for (int pos = stop + l; pos >= start; ) {
                pos = sb.lastIndexOf("&gt;", pos);
                if (pos < start) {
                    break;
                } // if
                sb.replace(pos, pos + 4, ">");
            } // for
        } // while

        return sb.toString();
    }

    /**
     * Checks if the returned Response <i>response</i> has a valid media type of "application/xml".
     *
     * @param response Response to check
     * @return true if valid else false
     */
    protected boolean isValidXMLResponseType(Response response) {
        if (response == null) {
            return false;
        } // if

        MediaType type = response.getMediaType();
        if (!type.isCompatible(MediaType.APPLICATION_XML_TYPE)) {
            log.warn("Incompatible mediatype (actual: \"{}\", expected: \"{}\") for request: {}",
                    type, MediaType.APPLICATION_XML_TYPE, response);
            return false;
        } // if

        return true;
    }

    /**
     * Creates a new WebTarget from the REST Client with the given <i>bookID</i>. This target can then be used to request the remote data.
     *
     * @param bookID ID of book, will be part of the path
     * @return {@link WebTarget}
     */
    protected WebTarget getWebTargetForBookID(String bookID) {
        return client
                .target(BOOK_URI)
                .path(bookID)
                .queryParam("key", API_KEY)
                .queryParam("format", "XML");
    }

    /**
     * Creates a new WebTarget from the REST Client with the given <i>authorID</i> and <i>page</i>. This target can then be used to request the remote data.
     *
     * @param authorID ID of author, will be part of the path
     * @param page     Page of paginated search result list
     * @return {@link WebTarget}
     */
    protected WebTarget getWebTargetForAuthorID(String authorID, int page) {
        return client
                .target(AUTHORS_BOOKS_URI)
                .path(authorID)
                .queryParam("key", API_KEY)
                .queryParam("page", String.valueOf(page));
    }

    /**
     * Creates a new WebTarget from the REST Client with the given <i>seriesID</i>. This target can then be used to request the remote data.
     *
     * @param seriesID ID of series, will be part of the path
     * @return {@link WebTarget}
     */
    protected WebTarget getWebTargetForSeriesID(String seriesID) {
        return client
                .target(SERIES_BOOKS_URI)
                .queryParam("key", API_KEY)
                .queryParam("id", seriesID);
    }

    /**
     * Creates a new WebTarget from the REST Client with the given <i>search term</i>. This target can then be used to request the remote data.
     *
     * @param searchTerm search term, will be part of the query parameter
     * @return {@link WebTarget}
     */
    protected WebTarget getWebTargetForSearchTerm(String searchTerm, int page) {
        return client
                .target(SEARCH_BOOKS_URI)
                .queryParam("key", API_KEY)
                .queryParam("q", searchTerm)
                .queryParam("page", String.valueOf(page));
    }

    @Override
    public void configureWith(Properties props) {
        if (props == null) {
            this.props = new Props();
        } else {
            if (props instanceof Props) {
                this.props = (Props) props;
            } else {
                this.props = new Props(props);
            } // if-else
        } // if-else

        // Get values and check
        this.API_KEY = this.props.getStringProp("goodreads.api.key", null);
        this.logRequests = this.props.getBoolProp("goodreads.api.log.requests", false);
    }

    @Override
    public boolean hasBeenInitialized() {
        return hasBeenInitialized;
    }

    @Override
    public void initialize() throws Exception {
        // If it has been initialized then it doesn't need to be called again.
        if (this.hasBeenInitialized) {
            return;
        } // if

        // TODO: initialization code

        // Check
        if (API_KEY == null) {
            throw new Exception("API_KEY was not set!");
        } // if

        // Create objects
        client = ClientBuilder.newClient();
        // TODO: register interceptor gzip/compression
        // Log requests if needed
        if (logRequests) {
            client.register(LoggingFilter.class);
        } // if

        this.hasBeenInitialized = true;
    }
}
