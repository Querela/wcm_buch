package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.AuthorInfo;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

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

        // Get author of the book
        String bookAuthor = book.getAuthors().get(0).getName();
        
        // Test out algorithm
        MapLanguageBookInfo mlbi = getLanguages(editionsID);

        // Convert java object to JSON ...
        String result = Utils.marshallObject(mlbi, false, true);
        // Output ...
        log.info("result:\n{}", result);
    }

    public void doAnotherTest() throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Get user input
        System.out.print("Titel: ");
        String titelToSearch = br.readLine();
        System.out.print("isGerman (-> type: yes) else English: ");
        String resp = br.readLine();
        boolean isGerman = "yes".equalsIgnoreCase(resp) || "y".equalsIgnoreCase(resp);
        log.info("isGerman:{}, title:\"{}\"", isGerman, titelToSearch);

        System.out.print("use only first result of hit (-> type: yes) else show all: ");
        resp = br.readLine();
        boolean useOnlyFirstResultOfHit = "yes".equalsIgnoreCase(resp) || "y".equalsIgnoreCase(resp);
        System.out.print("use all hits (-> type: yes) else first result overall (from first hit) : ");
        resp = br.readLine();
        boolean useOnlyTheFirstResultOverall = !("yes".equalsIgnoreCase(resp) || "y".equalsIgnoreCase(resp));

        System.out.print("show ES result (-> type: yes) else nothing: ");
        resp = br.readLine();
        boolean showESResult = "yes".equalsIgnoreCase(resp) || "y".equalsIgnoreCase(resp);

        System.out.println();
        System.out.println();
        System.out.println("Search for \"" + titelToSearch + "\" (in language: \"" + ((isGerman) ? "ger" : "eng") + "\")");
        System.out.println("Show " + ((useOnlyFirstResultOfHit) ? "only the first" : "each")
                + " result of " + ((useOnlyTheFirstResultOverall) ? "the first" : "each") + " hit.");
        System.out.println();
        System.out.println();

        // do ES search
        String searchTitle = titelToSearch.replace(" ", "+");
        WebTarget webTarget = ClientBuilder.newClient().target("http://" + HOST + "/dnb_db/_search?q=title:" + searchTitle + "&size=5&pretty=true");
        log.info("webTarget: {}", webTarget.getUri().toASCIIString());
        String result = webTarget.request().buildGet().invoke().readEntity(String.class);
        if (showESResult) {
            log.info("result:\n{}", result);
        } // if

        // Parse ES response
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(result);
        JSONObject hitsWrapper = (JSONObject) jsonObject.get("hits");
        JSONArray hits = (JSONArray) hitsWrapper.get("hits");

        boolean isFinished = false;
        for (int nr = 0; nr < hits.size() && !isFinished; nr++) {
            JSONObject hit = (JSONObject) hits.get(nr);
            JSONObject hitData = (JSONObject) hit.get("_source");

            // get data from search result
            String language = (String) hitData.get("language");
            String originalTitle = (String) hitData.get("original_title");
            String otherTitle = (String) hitData.get("title");
            log.info("ES Hit [{}]: lang:{}, orig:\"{}\", title:\"{}\"", nr, language, originalTitle, otherTitle);

            String dnbTitle = otherTitle;
            // if search in Goodreads is english
            if (!isGerman) {
                // get german title from dnb
                if (language != null && language.contains("ger")) { //title in dnb is german
                    dnbTitle = otherTitle;
                // otherwise get german title from dnb eng
                } else if (language != null && language.contains("eng")) {
                    dnbTitle = originalTitle;
                } // if-else
                log.info("German version of the book is called: \"{}\"", dnbTitle);
            } else if (isGerman) {
                // get english title from dnb
                if (language != null && language.contains("ger")) { // title in dnb is german
                    dnbTitle = originalTitle;
                } else {
                    dnbTitle = otherTitle;
                } // if-else
                log.info("English version of the book is called: \"{}\"", dnbTitle);
            }
            
//            StringBuilder dnbTitleAuthor;
//            dnbTitleAuthor.append(dnbTitle);
//            dnbTitleAuthor.append(" ");
//            dnbTitleAuthor.append(firstBookAuthor);
            
            // Search goodreads // ADD author e.g. "J.K. Rowling Harry Potter and the Goblet of Fire"
            SearchResultList srl = DataExtractor.getInstance().getSearchResults(dnbTitle, 1);

            boolean isFirst = true;
            for (Book bk : srl.getBooks()) {
                log.info("\t{}Found book: {}, id:{}", ((isFirst) ? "***" : ""), bk.getTitle(), bk.getGoodreadsID());
                if (useOnlyFirstResultOfHit) {
                    break;
                } else {
                    isFirst = false;
                } // if-else

                // TODO: check goodreads title etc.
                // TODO: ...

                // Add book after check to result list
                // // // Book bookInfo = DataExtractor.getInstance().getBook("" + bk.getGoodreadsID());
                if (useOnlyTheFirstResultOverall) {
                    isFinished = true;
                    if (useOnlyFirstResultOfHit) {
                        break;
                    } // if
                } // if
            } // for
            if (srl.getBooks() == null || srl.getBooks().isEmpty()) {
                log.info("\tNo books found ...");
            } // if
        } // for
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
                if (!isGerman) {
                    // get german title from dnb
                    if (language != null && language.contains("ger")) { //title in dnb is german
                        dnbTitle = otherTitle;
                    // otherwise get german title from dnb eng
                    } else if (language != null && language.contains("eng")) {
                        dnbTitle = originalTitle;
                    } // if-else
                    log.info("German version of the book is called: \"{}\"", dnbTitle);
                } else if (isGerman) {
                    // get english title from dnb
                    if (language != null && language.contains("ger")) { // title in dnb is german
                        dnbTitle = originalTitle;
                    } else {
                        dnbTitle = otherTitle;
                    } // if-else
                    log.info("English version of the book is called: \"{}\"", dnbTitle);
                }

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
//         dednbit.doTest();

        try {
            // Do interactive ...
            dednbit.doAnotherTest();
        } catch (Exception e) {
        } // try-catch

        DataCache.stop();
    }
}
