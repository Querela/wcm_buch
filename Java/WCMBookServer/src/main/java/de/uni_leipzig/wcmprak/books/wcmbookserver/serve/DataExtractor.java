package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.GoodreadsAPIAdapter;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.GoodreadsAPIResponseParser;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.GoodreadsScreenScraper;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.*;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String searchResultPage = grAPI.getDataForSearchTerm(searchTerm, page);
        SearchResultList searchResultList = grAPIPars.parseSearchResultData(searchResultPage, GOODREADS_BASE_URL);
        return searchResultList;
    }

    public SearchResultList getAllSearchResults(String searchTerm) {
        String[] searchPages = grAPI.getAllPagesDataForSearchTerm(searchTerm);

        String[] urls = new String[searchPages.length];
        for (int i = 0; i < searchPages.length; i++) urls[i] = GOODREADS_BASE_URL;

        SearchResultList searchResultList = grAPIPars.parseSearchResultData(searchPages, urls);
        return searchResultList;
    }

    public Book getBook(String bookID) {
        String bookContent = grAPI.getDataForBookID(bookID);
        Book book = grAPIPars.parseBookData(bookContent, GOODREADS_BASE_URL);
        return book;
    }

    public BookEditionsList getEditions(String editionsID) {
        return grSS.parseEditionsPage(String2Int(editionsID));
    }

    public MapLanguageBookInfo getLanguages(String editionsID) {
        BookEditionsList bel = getEditions(editionsID);
        MapLanguageBookInfo mlbi = new MapLanguageBookInfo(bel);

        return mlbi;
    }

    public SeriesInfo getSeries(String seriesID) {
        String seriesContent = grAPI.getDataForSeriesID(seriesID);
        SeriesInfo series = grAPIPars.parseSeriesData(seriesContent, GOODREADS_BASE_URL);
        return series;
    }

    public AuthorInfo getAuthorInfo(String authorID) {
        String[] authorsBooks = grAPI.getAllPagesDataForAuthorID(authorID);

        String[] urls = new String[authorsBooks.length];
        for (int i = 0; i < authorsBooks.length; i++) urls[i] = GOODREADS_BASE_URL;

        AuthorInfo authorInfo = grAPIPars.parseAuthorsBookData(authorsBooks, urls);
        return authorInfo;
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
