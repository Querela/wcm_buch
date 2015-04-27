package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.Book;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.BookEditionsList;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.MapLanguageBookInfo;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.SearchResultList;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Props;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

    // private final static String HOST = "ERIK-UBUNTU:9200";
    private final static String HOST = "localhost:9200";

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

        // Test language
        boolean isEnglish = bookLanguage != null && (bookLanguage.contains("eng") || bookLanguage.contains("en-US"));
        boolean isGerman = bookLanguage != null && bookLanguage.contains("ger");
        log.info("isEnglish:{}, isGerman:{}", isEnglish, isGerman);

        // Build and execute search
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client
                .target("http://" + HOST + "/dnb_db/_search?q=title:" + searchTitle + "&size=5&pretty=true");
                /*.path("dnb_ger") // .path("dnb_" + bookLanguage)
                .path("_search")
                .queryParam("q", "title:" + searchTitle)
                .queryParam("size", String.valueOf(5))
                .queryParam("pretty", true);*/
        log.info("webTarget: {}", webTarget.getUri().toASCIIString());
        Response response = webTarget.request().buildGet().invoke();
        String result = response.readEntity(String.class);
        log.info("result:\n{}", result);

        // parse elastic search results
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(result);
            JSONObject hitsWrapper = (JSONObject) jsonObject.get("hits");
            JSONArray hits = (JSONArray) hitsWrapper.get("hits");

            // TODO: iterate all or only the first
            for (int nr = 0; nr < hits.size(); nr++) {
                JSONObject hit = (JSONObject) hits.get(nr);
                JSONObject hitData = (JSONObject) hit.get("_source");

                // get data from search result
                String language = (String) hitData.get("language");
                String originalTitle = (String) hitData.get("original_title");
                String otherTitle = (String) hitData.get("title");
                log.info("Hit [{}]: lang:{}, orig:\"{}\", title:\"{}\"", nr, language, originalTitle, otherTitle);

                String dnbTitle = otherTitle;
                // if search in Goodreads is english
                if (isEnglish) {
                    // get german title from dnb
                    if (language != null && language.contains("ger")) {
                        dnbTitle = otherTitle;
                    } else {
                        log.info("No German edition in dnbDB found ...");
                    } // if-else
                } else if (isGerman) {
                    // get english title from dnb
                    if (language != null && language.contains("eng")) {
                        dnbTitle = otherTitle;
                    } else {
                        log.info("No English edition in dnbDB found ...");
                    } // if-else
                }
                log.info("translated \"{}\" version is called: \"{}\"", language, dnbTitle);

                // Search goodreads
                SearchResultList srl = DataExtractor.getInstance().getSearchResults(author + " " + dnbTitle, 1);
                if (srl.getBooks().isEmpty()) {
                    // if with author is empty try only the title
                    srl = DataExtractor.getInstance().getSearchResults(dnbTitle, 1);
                } // if

                for (Book bk : srl.getBooks()) {
                    log.info("Found book: {}, id:{}", bk.getTitle(), bk.getGoodreadsID());
                    // TODO: check goodreads title etc.
                    // TODO: ...

                    // Add book after check to result list
                    Book bookInfo = DataExtractor.getInstance().getBook("" + bk.getGoodreadsID());
                    mlbi.addBook(bookInfo);
                    break; // stop after first book // TODO: stop for all hits?
                } // for
                if (srl.getBooks() == null || srl.getBooks().isEmpty()) {
                    log.info("No books found ...");
                } // if
            } // for
        } catch (ParseException e) {
            log.error("json parsing error", e);
        } catch (Exception e) {
            log.error("json parsing", e);
        } // try-catch

        return mlbi;
    }


    public static void main(String[] args) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
        System.setProperty("org.slf4j.simpleLogger.levelInBrackets", "true");
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
