package de.uni_leipzig.wcmprak.books.wcmbookserver.extract;

import com.mongodb.*;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.AuthorInfo;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.Book;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.SeriesInfo;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.Shelf;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Configurable;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Initializable;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by Erik on 01.12.2014.
 */
public class GoodreadsMongoAdapter implements Configurable, Initializable {
    private final static Logger log = LoggerFactory.getLogger(GoodreadsMongoAdapter.class);

    private final static String DATABASE_NAME = "books";
    private final static String COLLECTION_NAME_BOOKS = "goodreads.books";
    private final static String COLLECTION_NAME_SERIES = "goodreads.series"; // ?

    private boolean hasBeenInitialized = false;
    private Props props = null;

    private MongoClient mongoClient;
    private DB db;
    private DBCollection collBooks;

    Object storeBook(Book book) {
        BasicDBObject obj = new BasicDBObject("goodreadsID", book.getGoodreadsID())
                .append("title", book.getTitle())
                .append("orignalTitle", book.getOriginalTitle())
                .append("url", book.getUrl())
                .append("imageUrl", book.getImageURL())
                .append("description", book.getDescription())
                .append("language", book.getLanguage());

        if (book.getSeries() != null) {
            SeriesInfo series = book.getSeries();
            obj.append("series", new BasicDBObject("goodreadsID", series.getGoodreadsID())
                    .append("name", book.getSeries().getName())
                    .append("description", series.getDescription())
                    .append("numberInSeries", book.getNumberInSeries())
                    .append("seriesCount", series.getNumberOfBooks()));
        } else {
            obj.append("series", null);
        } // if-else

        BasicDBList list = new BasicDBList();
        for (AuthorInfo author : book.getAuthors()) {
            list.add(new BasicDBObject("goodreadsID", author.getGoodreadsID())
                    .append("name", author.getName())
                    .append("url", author.getUrl()));
        } // for
        obj.append("authors", list);
        for (Shelf shelf : book.getShelves()) {
            list.add(new BasicDBObject("name", shelf.getName())
                    .append("count", shelf.getCount()));
        } // for
        obj.append("shelves", list);

        collBooks.insert(obj);
        return obj.get("_id");
    }

    Book loadBook(Object id) {
        return null;
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

        // Connect to MongoDB
        mongoClient = new MongoClient();
        db = mongoClient.getDB(DATABASE_NAME);

        collBooks = db.getCollection(COLLECTION_NAME_BOOKS);

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                // this.setName("ShutdownHook-mongo");
                log.debug("Run Shutdownhook mongo close");
                if (mongoClient != null) {
                    try {
                        mongoClient.close();
                    } catch (Exception e) {
                        log.error(e.getLocalizedMessage(), e);
                    } // try-catch
                } // if
            }
        }, "shutdownHook-mongo"));

        this.hasBeenInitialized = true;
    }
}
