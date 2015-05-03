package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.GoodreadsAPIAdapter;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.GoodreadsAPIResponseParser;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.GoodreadsScreenScraper;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.*;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Props;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.util.Properties;

/**
 * Created by Erik on 19.04.2015.
 */
public class DataExtractor {
    private final static Logger log = LoggerFactory.getLogger(DataExtractor.class);

    private static DataExtractor instance;
    private static Props props = null;

    private GoodreadsAPIAdapter grAPI;
    private GoodreadsAPIResponseParser grAPIPars;
    private GoodreadsScreenScraper grSS;

    private final static String GOODREADS_BASE_URL = "https://www.goodreads.com/";
    private static String ELASTICSEARCH_HOST = "localhost:9200";

    private DataExtractor() {
    }

    /**
     * Returns the single shared instance used for data extraction.
     *
     * @return {@link DataExtractor}
     */
    public static DataExtractor getInstance() {
        return instance;
    }

    public SearchResultList getSearchResults(String searchTerm, int page) {
        String key = "search:/" + searchTerm + ":/" + page;
        SearchResultList searchResultList = (SearchResultList) DataCache.getInstance().get(key);
        if (searchResultList != null) {
            return searchResultList;
        }

        String searchResultPage = grAPI.getDataForSearchTerm(searchTerm, page);
        searchResultList = grAPIPars.parseSearchResultData(searchResultPage, GOODREADS_BASE_URL);
        DataCache.getInstance().store(key, searchResultList);

        return searchResultList;
    }

    public SearchResultList getAllSearchResults(String searchTerm) {
        String key = "search:/" + searchTerm + ":/1";
        SearchResultList searchResultList = (SearchResultList) DataCache.getInstance().get(key);
        if (searchResultList != null) {
            return searchResultList;
        }

        String[] searchPages = grAPI.getAllPagesDataForSearchTerm(searchTerm);

        String[] urls = new String[searchPages.length];
        for (int i = 0; i < searchPages.length; i++) urls[i] = GOODREADS_BASE_URL;

        searchResultList = grAPIPars.parseSearchResultData(searchPages, urls);
        DataCache.getInstance().store(key, searchResultList);

        return searchResultList;
    }

    public Book getBook(String bookID) {
        String key = "book:/" + bookID;
        Book book = (Book) DataCache.getInstance().get(key);
        if (book != null) {
            return book;
        }

        String bookContent = grAPI.getDataForBookID(bookID);
        book = grAPIPars.parseBookData(bookContent, GOODREADS_BASE_URL);
        DataCache.getInstance().store(key, book);

        return book;
    }

    public BookEditionsList getEditions(String editionsID) {
        String key = "editions:/" + editionsID;
        BookEditionsList bookEditionsList = (BookEditionsList) DataCache.getInstance().get(key);
        if (bookEditionsList != null) {
            return bookEditionsList;
        }

        bookEditionsList = grSS.parseEditionsPage(String2Int(editionsID));
        DataCache.getInstance().store(key, bookEditionsList);

        return bookEditionsList;
    }

    public MapLanguageBookInfo getLanguages(String editionsID) {
        String key = "languages:/" + editionsID;
        MapLanguageBookInfo mlbi = (MapLanguageBookInfo) DataCache.getInstance().get(key);
        if (mlbi != null) {
            return mlbi;
        }

        BookEditionsList bel = getEditions(editionsID);
        mlbi = new MapLanguageBookInfo(bel);
        DataCache.getInstance().store(key, mlbi);

        return mlbi;
    }

    public SeriesInfo getSeries(String seriesID) {
        String key = "series:/" + seriesID;
        SeriesInfo seriesInfo = (SeriesInfo) DataCache.getInstance().get(key);
        if (seriesInfo != null) {
            return seriesInfo;
        }

        String seriesContent = grAPI.getDataForSeriesID(seriesID);
        seriesInfo = grAPIPars.parseSeriesData(seriesContent, GOODREADS_BASE_URL);
        DataCache.getInstance().store(key, seriesInfo);

        return seriesInfo;
    }

    public AuthorInfo getAuthorInfo(String authorID) {
        String key = "author:/" + authorID;
        AuthorInfo authorInfo = (AuthorInfo) DataCache.getInstance().get(key);
        if (authorInfo != null) {
            return authorInfo;
        }

        String[] authorsBooks = grAPI.getAllPagesDataForAuthorID(authorID);

        String[] urls = new String[authorsBooks.length];
        for (int i = 0; i < authorsBooks.length; i++) urls[i] = GOODREADS_BASE_URL;

        authorInfo = grAPIPars.parseAuthorsBookData(authorsBooks, urls);
        DataCache.getInstance().store(key, authorInfo);

        return authorInfo;
    }

    /**
     * Query ElasticSearch for translated title.
     *
     * @param otherTitle title of book - we search for translated title
     * @param language   original language of book
     * @param author     original author of book - currently ignored?
     * @return String with translated title or null (if not found or on error)
     */
    public String getDNBTitle(String otherTitle, String language, String author) {
        log.debug("Search for \"{}\" (in language: \"{}\") (with author: \"{}\")", otherTitle, language, author);
        boolean isGerman = language.contains("ger");

        // do ES DNB search
        String searchTitle = otherTitle.replace(" ", "+");
        WebTarget webTarget = ClientBuilder.newClient().target("http://" + ELASTICSEARCH_HOST + "/dnb_db/_search?q=title:" + searchTitle + "&size=5&pretty=true");
        log.debug("webTarget: {}", webTarget.getUri().toASCIIString());
        String result = webTarget.request().buildGet().invoke().readEntity(String.class);

        // Parse ES DNB response
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(result);
            JSONObject hitsWrapper = (JSONObject) jsonObject.get("hits");
            JSONArray hits = (JSONArray) hitsWrapper.get("hits");

            boolean isFinished = false;
            for (int nr = 0; nr < hits.size() && !isFinished; nr++) {
                JSONObject hit = (JSONObject) hits.get(nr);
                JSONObject hitData = (JSONObject) hit.get("_source");

                // get data from search result
                String hit_language = (String) hitData.get("language");
                String hit_originalTitle = (String) hitData.get("original_title");
                String hit_otherTitle = (String) hitData.get("title");
                log.info("ES Hit [{}]: lang:{}, orig:\"{}\", title:\"{}\"", nr, hit_language, hit_originalTitle, hit_otherTitle);

                String dnbTitle = hit_otherTitle;
                // if search in Goodreads is english
                if (!isGerman) {
                    // get german title from dnb
                    if (hit_language != null && hit_language.contains("ger")) { //title in dnb is german
                        dnbTitle = hit_otherTitle;
                        // otherwise get german title from dnb eng
                    } else if (hit_language != null && hit_language.contains("eng")) {
                        dnbTitle = hit_originalTitle;
                    } // if-else
                    log.info("German version of the book is called: \"{}\"", dnbTitle);
                } else if (isGerman) {
                    // get english title from dnb
                    if (hit_language != null && hit_language.contains("ger")) { // title in dnb is german
                        dnbTitle = hit_originalTitle;
                    } else {
                        dnbTitle = hit_otherTitle;
                    } // if-else
                    log.info("English version of the book is called: \"{}\"", dnbTitle);
                } // if-else

                return dnbTitle;
            } // for
        } catch (Exception e) {
            return null;
        } // try-catch

        return null;
    }

    /**
     * Convert String to int.
     *
     * @param value String to convert
     * @return int
     */
    private static int String2Int(String value) {
        int val = 0;
        try {
            val = Integer.parseInt(value);
        } catch (Exception e) {
            throw e;
        }
        return val;
    }

    /**
     * Set configuration for data extraction.
     *
     * @param props standard java properties
     */
    public static void configureWith(Properties props) {
        if (props == null) {
            DataExtractor.props = new Props();
        } else {
            if (props instanceof Props) {
                DataExtractor.props = (Props) props;
            } else {
                DataExtractor.props = new Props(props);
            } // if-else
        } // if-else

        ELASTICSEARCH_HOST = DataExtractor.props.getStringProp("es.host", ELASTICSEARCH_HOST);
    }

    /**
     * Initialize instance for data extraction.
     *
     * @throws Exception
     */
    public static void initialize() throws Exception {
        // Create new instance object
        instance = new DataExtractor();

        // initialize modules of the instance
        instance.grAPI = new GoodreadsAPIAdapter();
        instance.grAPIPars = new GoodreadsAPIResponseParser();
        instance.grSS = new GoodreadsScreenScraper();

        // set configurations for modules
        instance.grAPI.configureWith(props);
        instance.grAPIPars.configureWith(props);
        instance.grSS.configureWith(props);

        // initialize modules
        instance.grAPI.initialize();
        instance.grAPIPars.initialize();
        instance.grSS.initialize();
    }
}
