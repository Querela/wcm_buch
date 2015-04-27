package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.Book;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.BookEditionsList;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.MapLanguageBookInfo;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Erik on 22.04.2015.
 */
public class DataExtractorDNBImplTest {
    private final static Logger log = LoggerFactory.getLogger(DataExtractorDNBImplTest.class);

    public DataExtractorDNBImplTest() {

    }

    public void doTest() {
        // ?
        String bookID = "3"; // for Harry Potter #1
        Book book = DataExtractor.getInstance().getBook(bookID);

        // Get editions ID somehow
        String editionsID = "" + book.getGoodreadsEditionsID(); // use harry potter editions id

        // Test out algorithm
        MapLanguageBookInfo mlbi = getLanguages(editionsID);

        // Convert java object to JSON ...
        String result = Utils.marshallObject(mlbi, true, true);
        // Output ...
        log.info("result:\n{}", result);
    }

    public MapLanguageBookInfo getLanguages(String editionsID) {
        BookEditionsList bel = DataExtractor.getInstance().getEditions(editionsID);
        bel.getBooks().clear(); // clear existing books ...?
        MapLanguageBookInfo mlbi = new MapLanguageBookInfo(bel); // copy constructor

        String bookID = "" + mlbi.getMainBookGoodreadsID();
        String author = DataExtractor.getInstance().getBook(bookID).getAuthors().get(0).getName();
        String title = mlbi.getMainBookTitle();
        log.info("bookID: \"{}\", autor: \"{}\", title: \"{}\"", bookID, author, title);

        // TODO: lets do our magic here ...
        // TODO: add dnb query etc.

        return mlbi;
    }


    public static void main(String[] args) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.log.de.uni_leipzig.wcmprak.books.wcmbookserver", "debug"); // TODO: remove in final version
        System.setProperty("org.slf4j.simpleLogger.log.de.uni_leipzig.wcmprak.books.wcmbookserver.serve.DataExtractorDNBImplTest", "debug"); // show everything

        // Configure and initialize DataExtractor
        Props props = new Props();
        props.setStringProp("goodreads.api.key", "RwUzZwkv94PCodD1lMF5g");
        DataExtractor.configureWith(props);
        DataExtractor.initialize();

        // Do our test impl method
        DataExtractorDNBImplTest dednbit = new DataExtractorDNBImplTest();
        dednbit.doTest();
    }
}
