package de.uni_leipzig.wcmprak.books.wcmbookserver.extract;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.AuthorInfo;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.BookEditionInfo;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.BookEditionsList;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Configurable;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Initializable;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.JSoup;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Props;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Erik on 03.12.2014.
 */
public class GoodreadsScreenScraper implements Configurable, Initializable {
    /**
     * User Agent for more authentic requests
     */
    public final static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:33.0) Gecko/20100101 Firefox/33.0";
    /**
     * Maximal web page download file size for Jsoup. Zero (0) for unlimited. Current: 10 MB, Default 1 MB.
     */
    public final static int MAX_BODY_SIZE = 10 * 1024 * 1024;
    private final static Logger log = LoggerFactory.getLogger(GoodreadsAPIAdapter.class);
    private final static String BASE_URI = "https://www.goodreads.com/";
    private final static String EDITIONS_URI = BASE_URI + "work/editions/";
    private final static String EDITIONS_URI_EXT = "?utf8=✓&per_page=100&page=";

    private final static Pattern PATTERN_PAGINATION = Pattern.compile("\\(showing (?<start>\\d+)\\-(?<end>\\d+) of (?<total>\\d+)\\)");
    private final static Pattern PATTERN_BOOK_PUBLISH = Pattern.compile("Published (?<publishDate>.+?) by (?<publisher>.+)", Pattern.UNICODE_CASE | Pattern.DOTALL);
    // private final static SimpleDateFormat FORMAT_PUBLISH_DATE = new SimpleDateFormat("MMMM d yyyy");

    private boolean hasBeenInitialized = false;
    private Props props = null;
    private boolean logRequests = false;

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

        // TODO: more
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

        this.hasBeenInitialized = true;
    }

    /**
     * Retrieves the web pages associated with the editions id from goodreads, parses them and returns nice wrapper objects.
     *
     * @param editionsID edition id of a book
     * @return {@link BookEditionsList} with parsed data or null on error
     */
    public BookEditionsList parseEditionsPage(int editionsID) {
        log.debug("Request and parse content for edition id={} (all pages) ...", editionsID);
        String url = getEditionsUrlFromID(editionsID, 1);

        Document doc = getDOM(url);
        if (doc == null) {
            return null;
        } // if

        // Check pagination for other editions/pages
        int countPages = 1;
        String pagination_info = JSoup.getElementValue(doc.select("html > body > div.content > div.mainContentContainer > div.mainContent > div.mainContentFloat > div.workEditions > div.editionsSecondHeader > div.workInfo > div.showingPages > span"));
        if (pagination_info == null) {
            log.warn("No pagination info found for edition id={}", editionsID);
        } else {
            Matcher pmatcher = PATTERN_PAGINATION.matcher(pagination_info);
            if (!pmatcher.matches()) {
                log.warn("Couldn't parse pagination info for edition id={}", editionsID);
            } else {
                try {
                    int start = Integer.valueOf(pmatcher.group("start"));
                    int end = Integer.valueOf(pmatcher.group("end"));
                    int total = Integer.valueOf(pmatcher.group("total"));

                    log.debug("First result page: Results {} - {} of {}", start, end, total);
                    int resultsPerPage = end - start + 1;
                    countPages = ((total + resultsPerPage - 1) / resultsPerPage);
                    log.debug("Total pages to request: {}, pages left: {}", countPages, countPages - 1);
                } catch (Exception e) {
                    // ignore
                } // try-catch
            } // if-else
        } // if-else

        BookEditionsList list = new BookEditionsList();
        list.setEditionsID(editionsID);

        Element main_ele = JSoup.getSingleElement(doc.select("div.mainContentFloat"));

        // Parse book edition info
        Element ele = JSoup.getSingleElement(main_ele.select("h1 > a"));
        list.setMainBookTitle(JSoup.getElementValue(ele));
        list.setMainBookGoodreadsID(getGoodreadsIDFromUrl(JSoup.getAttributeValue(ele, "href")));

        // Parse edition author info
        ele = JSoup.getSingleElement(main_ele.select("div.workEditions > h2 > a"));
        AuthorInfo author = new AuthorInfo();
        author.setName(JSoup.getElementValue(ele));
        author.setUrl(JSoup.getAttributeValue(ele, "href"));
        author.setGoodreadsID(getGoodreadsIDFromUrl(author.getUrl()));
        list.setMainAuthor(author);

        // Append books
        for (BookEditionInfo book : parseEditionsPageBooks(doc)) {
            list.addBook(book);
        } // for
        // Append books from following pages
        for (int page_num = 2; page_num <= countPages; page_num++) {
            url = getEditionsUrlFromID(editionsID, page_num);
            log.debug("Parse books from edition page: {}", url);
            doc = getDOM(url);

            for (BookEditionInfo book : parseEditionsPageBooks(doc)) {
                list.addBook(book);
            } // for
        } // for

        return list;
    }

    /**
     * Parse all edition books from a given page DOM.
     *
     * @param doc DOM of web page of book editions
     * @return list of {@link BookEditionInfo}
     */
    protected static List<BookEditionInfo> parseEditionsPageBooks(Document doc) {
        List<BookEditionInfo> books = new ArrayList<>();

        if (doc == null) {
            return books;
        } // if

        Elements eles = doc.select("div.mainContentFloat > div.workEditions > div.elementList");
        if (eles == null || eles.isEmpty()) {
            log.warn("Couldn't find edition books in DOM ...");
            return books;
        } // if

        for (Element element : eles) {
            BookEditionInfo book = parseEditionBook(element);
            if (book != null) {
                books.add(book);
            } // if
        } // for

        return books;
    }

    /**
     * Parses a DOM element into a {@link BookEditionInfo}.
     *
     * @param element DOM data
     * @return {@link BookEditionInfo} or null on error
     */
    protected static BookEditionInfo parseEditionBook(Element element) {
        BookEditionInfo book = new BookEditionInfo();

        book.setImageUrl(JSoup.getAttributeValue(element.select("div.leftAlignedImage img"), "src"));
        book.setTitle(JSoup.getElementValue(element.select("div.editionData a.bookTitle")));
        book.setUrl(JSoup.getAttributeValue(element.select("div.editionData a.bookTitle"), "href"));
        book.setGoodreadsID(getGoodreadsIDFromUrl(book.getUrl()));

        String testKeyRating = JSoup.getElementValue(element.select("div.editionData > div.moreDetails > div.dataRow:eq(2) > div.dataTitle"));
        String testKeyISBN = JSoup.getElementValue(element.select("div.editionData > div.moreDetails > div.dataRow:eq(1) > div.dataTitle"));

        if (testKeyRating != null && testKeyRating.contains("rating")) {
            if (testKeyISBN != null && testKeyISBN.contains("ISBN")) {
                log.debug("Found no language for {} ...", book.getGoodreadsID());
                book.setLanguage(null);
            } else {
                book.setLanguage(JSoup.getElementValue(element.select("div.editionData > div.moreDetails > div.dataRow:eq(1) > div.dataValue")));
            } // if-else
        } else {
            book.setLanguage(JSoup.getElementValue(element.select("div.editionData > div.moreDetails > div.dataRow:eq(2) > div.dataValue")));
        } // if-else

        String publish = JSoup.getElementValue(element.select("div.editionData > div.dataRow:eq(1)"));
        Matcher pmatcher = PATTERN_BOOK_PUBLISH.matcher(publish);
        if (pmatcher.matches()) {
            book.setPublisher(pmatcher.group("publisher"));
            book.setPublishingDate(pmatcher.group("publishDate"));
        } // if

        for (Element ele : element.select("div.editionData > div.moreDetails > div.dataRow:eq(0) > div.dataValue > span > a.authorName")) {
            AuthorInfo author = new AuthorInfo();

            author.setUrl(JSoup.getAttributeValue(ele, "href"));
            author.setGoodreadsID(getGoodreadsIDFromUrl(author.getUrl()));
            author.setName(JSoup.getElementValue(ele));

            // TODO: get role/type
            Node n = ele.nextSibling();
            if (n != null) {
                n = n.nextSibling();
                if (n != null && "span".equalsIgnoreCase(n.nodeName())) {
                    String role = JSoup.getElementValue((Element) n);
                    log.debug("{} with role {} for book id {}", author.getName(), role, book.getGoodreadsID());
                    author.setRole(role);
                } // if
            } // if

            book.addAuthor(author);
        } // for

        return book;
    }

    /**
     * Extracts an id from a goodreads url or returns 0 on error
     *
     * @param url Url to parse
     * @return id or 0 on error
     */
    protected static int getGoodreadsIDFromUrl(String url) {
        if (url == null) {
            return 0;
        } // if

        int i = url.lastIndexOf('/');
        if (i == -1) {
            return 0;
        } // if

        url = url.substring(i + 1);

        i = url.indexOf('.');
        if (i != -1) {
            url = url.substring(0, i);
        } else {
            i = url.indexOf('-');
            if (i != -1) {
                url = url.substring(0, i);
            } // if
        } // if-else

        try {
            return Integer.valueOf(url);
        } catch (Exception e) {
            log.error("Got no id for url=" + url);
            return 0;
        } // try-catch
    }

    /**
     * Builds an url with the given editions id from goodreads.
     *
     * @param editionsID id of edition
     * @return String with url
     */
    protected static String getEditionsUrlFromID(int editionsID, int page) {
        if (page <= 0) {
            page = 1;
        } // if

        // concatenate id with url
        return EDITIONS_URI + editionsID + EDITIONS_URI_EXT + page;
    }

    /**
     * Downloads a web page with Jsoup. Sets various parameters.
     *
     * @param url URL of web page to download
     * @return DOM of web page or null on error
     */
    protected static Document getDOM(String url) {
        if (url == null || url.trim().length() == 0) {
            return null;
        } // if

        try {
            Document doc = Jsoup.connect(url)
                    // Set UserAgent & Timeout & max. Download size
                    .userAgent(USER_AGENT).timeout(5000).maxBodySize(MAX_BODY_SIZE)
                    .get();
            return doc;
        } catch (Exception e) {
            log.error("JSoup document download error", e);
        } // try-catch

        return null;
    }
}
