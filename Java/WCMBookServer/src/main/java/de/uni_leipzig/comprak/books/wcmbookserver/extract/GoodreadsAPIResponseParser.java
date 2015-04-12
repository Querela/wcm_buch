package de.uni_leipzig.comprak.books.wcmbookserver.extract;

import de.uni_leipzig.comprak.books.wcmbookserver.extract.data.AuthorInfo;
import de.uni_leipzig.comprak.books.wcmbookserver.extract.data.Book;
import de.uni_leipzig.comprak.books.wcmbookserver.extract.data.SeriesInfo;
import de.uni_leipzig.comprak.books.wcmbookserver.extract.data.Shelf;
import de.uni_leipzig.comprak.books.wcmbookserver.extract.utils.Configurable;
import de.uni_leipzig.comprak.books.wcmbookserver.extract.utils.Initializable;
import de.uni_leipzig.comprak.books.wcmbookserver.extract.utils.Props;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static de.uni_leipzig.comprak.books.wcmbookserver.extract.utils.JSoup.*;

/**
 * Created by Erik on 01.12.2014.
 */
public class GoodreadsAPIResponseParser implements Configurable, Initializable {
    private final static Logger log = LoggerFactory.getLogger(GoodreadsAPIResponseParser.class);

    private boolean hasBeenInitialized = false;
    private Props props = null;

    public Book parseBookData(String data, String url) {
        if (data == null) {
            return null;
        } // if

        Document doc = Jsoup.parse(data, url, Parser.xmlParser());
        if (doc == null) {
            return null;
        } // if

        // Check for right document format
        if (!"book_show".equals(getElementValue(doc.select("GoodreadsResponse > Request > method")))) {
            return null;
        } // if

        // TODO: add content // SAX?

        Book book = new Book();

        book.setGoodreadsID(Integer.parseInt(getElementValue(doc.select("GoodreadsResponse > book > id"))));
        book.setGoodreadsEditionsID(Integer.parseInt(getElementValue(doc.select("GoodreadsResponse > book > work > id"))));
        book.setTitle(getElementValue(doc.select("GoodreadsResponse > book > title")));
        book.setOriginalTitle(getElementValue(doc.select("GoodreadsResponse > book > work > original_title")));
        book.setLanguage(getElementValue(doc.select("GoodreadsResponse > book > language_code")));
        book.setDescription(getElementValue(doc.select("GoodreadsResponse > book > description")));
        book.setUrl(getElementValue(doc.select("GoodreadsResponse > book > url")));
        book.setImageURL(getElementValue(doc.select("GoodreadsResponse > book > image_url")));

        for (Element ele : doc.select("GoodreadsResponse > book > authors > author")) {
            AuthorInfo author = new AuthorInfo();
            author.setGoodreadsID(Integer.parseInt(getElementValue(ele.select("id"))));
            author.setName(getElementValue(ele.select("name")));
            author.setUrl(getElementValue(ele.select("link")));
            book.addAuthor(author);
        } // for

        for (Element ele : doc.select("GoodreadsResponse > book > popular_shelves > shelf")) {
            Shelf shelf = new Shelf();
            shelf.setName(getAttributeValue(ele, "name"));
            shelf.setCount(Integer.parseInt(getAttributeValue(ele, "count")));
            book.addShelf(shelf);
        } // for

        SeriesInfo series = new SeriesInfo();
        Element ele = getSingleElement(doc.select("GoodreadsResponse > book > series_works > series_work"));
        book.setNumberInSeries(Integer.parseInt(getElementValue(ele.select("user_position"))));
        ele = ele.select("series").first();
        series.setGoodreadsID(Integer.parseInt(getElementValue(ele.select("id"))));
        series.setName(getElementValue(ele.select("title")));
        series.setDescription(getElementValue(ele.select("description")));
        series.setNumberOfBooks(Integer.parseInt(getElementValue(ele.select("primary_work_count"))));
        book.setSeries(series);

        return book;
    }

    public AuthorInfo parseAuthorsBookData(String data, String url) {
        if (data == null) {
            return null;
        } // if

        Document doc = Jsoup.parse(data, url, Parser.xmlParser());
        if (doc == null) {
            return null;
        } // if

        // Check for right document format
        if (!"author_list".equals(getElementValue(doc.select("GoodreadsResponse > Request > method")))) {
            return null;
        } // if

        AuthorInfo authorsBooks = new AuthorInfo();

        authorsBooks.setGoodreadsID(Integer.parseInt(getElementValue(doc.select("GoodreadsResponse > author > id"))));
        authorsBooks.setName(getElementValue(doc.select("GoodreadsResponse > author > name")));
        authorsBooks.setUrl(getElementValue(doc.select("GoodreadsResponse > author > link")));

        for (Element bookEle : doc.select("GoodreadsResponse > author > books > book")) {
            Book book = new Book();
            book.setGoodreadsID(Integer.parseInt(getElementValue(bookEle.select("id"))));
            book.setTitle(getElementValue(bookEle.select("title")));
            book.setUrl(getElementValue(bookEle.select("link")));
            book.setImageURL(getElementValue(bookEle.select("image_url")));
            book.setDescription(getElementValue(bookEle.select("description")));

            // TODO: needed?
            // TODO: Check authors
            // TODO: Remove to avoid cyclic dependencies if serialised?
            for (Element authorEle : bookEle.select("authors > author")) {
                int id = Integer.parseInt(getElementValue(authorEle.select("id")));
                if (id == authorsBooks.getGoodreadsID()) {
                    // Add the same author like the container element
                    // book.addAuthor(authorsBooks);
                } else {
                    // Add only different author if IDs don't match
                    AuthorInfo author = new AuthorInfo();
                    author.setGoodreadsID(Integer.parseInt(getElementValue(authorEle.select("id"))));
                    author.setName(getElementValue(authorEle.select("name")));
                    author.setUrl(getElementValue(authorEle.select("link")));
                } // if-else
            } // for

            authorsBooks.addBook(book);
        } // for

        return authorsBooks;
    }

    public AuthorInfo parseAuthorsBookData(String[] datas, String[] urls) {
        if (datas == null || urls == null || (datas.length != urls.length)) {
            return null;
        } // if

        AuthorInfo[] authorsBookss = new AuthorInfo[datas.length];
        // Parse each file/page
        for (int idx = 0; idx < datas.length; idx++) {
            authorsBookss[idx] = parseAuthorsBookData(datas[idx], urls[idx]);
        } // for

        // merge results
        return mergeMultipleAuthorsBookDataPages(authorsBookss);
    }

    /**
     * Merges multiple <i>{@link de.uni_leipzig.comprak.books.wcmbookserver.extract.data.AuthorInfo}</i> object into a single one -> i. e. move all books to the first object.
     *
     * @param authorsBookInfos Array of <i>{@link de.uni_leipzig.comprak.books.wcmbookserver.extract.data.AuthorInfo}</i> objects whose books should be concatenated
     * @return {@link de.uni_leipzig.comprak.books.wcmbookserver.extract.data.AuthorInfo} with all the books from the other objects
     */
    protected AuthorInfo mergeMultipleAuthorsBookDataPages(AuthorInfo[] authorsBookInfos) {
        if (authorsBookInfos == null || authorsBookInfos.length == 0) {
            return null;
        } // if

        // TODO: check same author?

        AuthorInfo authorBook = authorsBookInfos[0];

        // Add other books to first book collection
        for (int idx = 1; idx < authorsBookInfos.length; idx++) {
            authorBook.getBooks().addAll(authorsBookInfos[idx].getBooks());
        } // for

        return authorBook;
    }

    public SeriesInfo parseSeriesData(String data, String url) {
        if (data == null) {
            return null;
        } // if

        Document doc = Jsoup.parse(data, url, Parser.xmlParser());
        if (doc == null) {
            return null;
        } // if

        // Check for right document format
        if (!"series_show".equals(getElementValue(doc.select("GoodreadsResponse > Request > method")))) {
            return null;
        } // if

        SeriesInfo series = new SeriesInfo();

        series.setGoodreadsID(Integer.parseInt(getElementValue(doc.select("GoodreadsResponse > series > id"))));
        series.setName(getElementValue(doc.select("GoodreadsResponse > series > title")));
        series.setDescription(getElementValue(doc.select("GoodreadsResponse > series > description")));
        series.setNumberOfBooks(Integer.parseInt(getElementValue(doc.select("GoodreadsResponse > series > primary_work_count"))));

        for (Element bookEle : doc.select("GoodreadsResponse > series > series_works > series_work")) {
            Book book = new Book();
            book.setGoodreadsID(Integer.parseInt(getElementValue(bookEle.select("work > best_book > id"))));
            book.setGoodreadsEditionsID(Integer.parseInt(getElementValue(bookEle.select("work > id"))));
            book.setTitle(getElementValue(bookEle.select("work > best_book > title")));

            // Set position in series
            float pos = -1;
            try {
                pos = Float.parseFloat(getElementValue(bookEle.select("user_position")));
            } catch (Exception e) {
            } finally {
                book.setNumberInSeries(pos);
            } // try-catch

            AuthorInfo author = new AuthorInfo();
            author.setGoodreadsID(Integer.parseInt(getElementValue(bookEle.select("work > best_book > author > id"))));
            author.setName(getElementValue(bookEle.select("work > best_book > author > name")));
            book.addAuthor(author);

            series.addBook(book);
        } // for

        return series;
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

        // SAXParserFactory spf = SAXParserFactory.newInstance();
        // SAXParser parser = spf.newSAXParser();
        // XMLReader reader = parser.getXMLReader();
        // reader.setContentHandler(null);

        this.hasBeenInitialized = true;
    }
}
