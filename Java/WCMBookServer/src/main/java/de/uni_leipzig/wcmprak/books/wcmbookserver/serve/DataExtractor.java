package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.GoodreadsAPIAdapter;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.GoodreadsAPIResponseParser;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.GoodreadsScreenScraper;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.*;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Configurable;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Initializable;
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
public class DataExtractor implements Configurable, Initializable {
    private final static Logger log = LoggerFactory.getLogger(DataExtractor.class);

    private static DataExtractor instance = null;
    private Props props = null;

    private boolean hasBeenInitialized = false;
    private String elasticSearchHost = ELASTICSEARCH_HOST;

    private GoodreadsAPIAdapter grAPI;
    private GoodreadsAPIResponseParser grAPIPars;
    private GoodreadsScreenScraper grSS;

    public final static String PROP_KEY_ES_HOST = "es.host";
    private final static String GOODREADS_BASE_URL = "https://www.goodreads.com/";
    private final static String ELASTICSEARCH_HOST = "localhost:9200";

    private DataExtractor() {
    }

    /**
     * Returns the single shared instance used for data extraction.
     *
     * @return {@link DataExtractor}
     */
    public synchronized static DataExtractor getInstance() {
        if (instance == null) {
            instance = new DataExtractor();
        } // if

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

        elasticSearchHost = this.props.getStringProp(PROP_KEY_ES_HOST, elasticSearchHost);
    }

    @Override
    public boolean hasBeenInitialized() {
        return hasBeenInitialized;
    }

    /**
     * Initialize instance for data extraction.
     *
     * @throws Exception
     */
    @Override
    public void initialize() throws Exception {
        // initialize modules of the instance
        grAPI = new GoodreadsAPIAdapter();
        grAPIPars = new GoodreadsAPIResponseParser();
        grSS = new GoodreadsScreenScraper();

        // set configurations for modules
        grAPI.configureWith(props);
        grAPIPars.configureWith(props);
        grSS.configureWith(props);

        // initialize modules
        grAPI.initialize();
        grAPIPars.initialize();
        grSS.initialize();

        hasBeenInitialized = true;
    }
}
