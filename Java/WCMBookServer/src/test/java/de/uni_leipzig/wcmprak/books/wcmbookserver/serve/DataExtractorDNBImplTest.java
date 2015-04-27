package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.Book;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.BookEditionsList;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.MapLanguageBookInfo;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

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
        String result = Utils.marshallObject(mlbi, false, true);
        // Output ...
        log.info("result:\n{}", result);
    }

    public MapLanguageBookInfo getLanguages(String editionsID) {
        BookEditionsList bel = DataExtractor.getInstance().getEditions(editionsID);
        bel.getBooks().clear(); // clear existing books ...?
        MapLanguageBookInfo mlbi = new MapLanguageBookInfo(bel); // copy constructor

        String bookID = "" + mlbi.getMainBookGoodreadsID();
        Book book = DataExtractor.getInstance().getBook(bookID);
        String author = book.getAuthors().get(0).getName();

        String title = mlbi.getMainBookTitle();
        log.info("bookID: \"{}\", author: \"{}\", title: \"{}\"", bookID, author, title);

        // Parameters for search
        String bookLanguage = book.getLanguage();
        String searchTitle = title
                .replace(" ", "+")
                .replace("'", "");
        log.info("bookLanguage: \"{}\", searchTitle: \"{}\"", bookLanguage, searchTitle);

        // Build and execute search
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client
                .target("http://ERIK-UBUNTU:9200/dnb_ger/_search?q=title:" + searchTitle + "&size=5&pretty=true");
                /*.path("dnb_ger") // .path("dnb_" + bookLanguage)
                .path("_search")
                .queryParam("q", "title:" + searchTitle)
                .queryParam("size", String.valueOf(5))
                .queryParam("pretty", true);*/
        log.info("webTarget: {}", webTarget.getUri().toASCIIString());
        Response response = webTarget.request().buildGet().invoke();
        String result = response.readEntity(String.class);
        log.info("result:\n{}", result);

        return mlbi;
    }


    public static void main(String[] args) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.log.de.uni_leipzig.wcmprak.books.wcmbookserver", "info"); // TODO: remove in final version
        System.setProperty("org.slf4j.simpleLogger.log.de.uni_leipzig.wcmprak.books.wcmbookserver.serve.DataExtractorDNBImplTest", "debug"); // show everything

        // Configure and initialize DataExtractor
        Props props = new Props();
        props.setStringProp("goodreads.api.key", "RwUzZwkv94PCodD1lMF5g");
        DataCache.configureWith(props);
        DataCache.initialize();
        DataExtractor.configureWith(props);
        DataExtractor.initialize();

        // Do our test impl method
        DataExtractorDNBImplTest dednbit = new DataExtractorDNBImplTest();
        dednbit.doTest();

        DataCache.stop();
    }
}
