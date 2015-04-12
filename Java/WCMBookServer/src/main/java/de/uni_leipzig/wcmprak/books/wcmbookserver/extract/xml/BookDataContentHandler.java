package de.uni_leipzig.wcmprak.books.wcmbookserver.extract.xml;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.AuthorInfo;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.Book;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.SeriesInfo;
import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data.Shelf;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Created by Erik on 04.12.2014.
 */
public class BookDataContentHandler implements ContentHandler {

    private StringBuffer sb = null;
    private boolean newStartTag = false;
    private boolean ignore = false;

    private Book book = null;
    private AuthorInfo author = null;
    private Shelf shelf = null;
    private SeriesInfo series = null;

    private boolean isInWork = false;
    private boolean isInAuthors = false;
    private boolean isInShelves = false;
    private boolean isInSeries = false;
    private boolean isInSeriesInner = false;

    public BookDataContentHandler() {
        reset();
    }

    public void reset() {
        sb = new StringBuffer();

        book = new Book();

        author = new AuthorInfo();
        shelf = new Shelf();
        series = new SeriesInfo();
    }

    public Book getBook() {
        return book;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (ignore) {
            return;
        } // if

        sb.delete(0, sb.length()).trimToSize();
        newStartTag = true;

        if ("work".equals(qName)) {
            isInWork = true;
        } else if ("authors".equals(qName)) {
            isInAuthors = true;
        } else if ("popular_shelves".equals(qName)) {
            isInShelves = true;
        } else if ("series_work".equals(qName)) {
            isInSeries = true;
        } else if ("series".equals(qName)) {
            isInSeriesInner = true;
        } else if ("reviews_widget".equals(qName) || "book_links".equals(qName) || "similar_books".equals(qName)) {
            ignore = true;
        } // if-else

        if (isInShelves) {
            if ("shelf".equals(qName)) {
                shelf.setName(atts.getValue("name"));
                shelf.setCount(Integer.parseInt(atts.getValue("count")));
                book.addShelf(shelf);
                shelf = new Shelf();
            } // if
        } // if
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (ignore) {
            if ("reviews_widget".equals(qName) || "book_links".equals(qName) || "similar_books".equals(qName)) {
                ignore = false;
            } // if
            return;
        } // if

        newStartTag = false;

        if (isInSeries) {
            if ("series_work".equals(qName)) {
                book.setSeries(series);
            } // if
        } // if
        if (isInAuthors) {
            if ("author".equals(qName)) {
                book.addAuthor(author);
                author = new AuthorInfo();
            } // if
        } // if

        if ("work".equals(qName)) {
            isInWork = false;
        } else if ("authors".equals(qName)) {
            isInAuthors = false;
        } else if ("popular_shelves".equals(qName)) {
            isInShelves = false;
        } else if ("series_work".equals(qName)) {
            isInSeries = false;
        } else if ("series".equals(qName)) {
            isInSeriesInner = false;
        } // if-else

        if ("id".equals(qName)) {
            if (isInSeriesInner) {
                series.setGoodreadsID(Integer.parseInt(sb.toString()));
            } else if (isInAuthors) {
                author.setGoodreadsID(Integer.parseInt(sb.toString()));
            } else if (!isInSeries && !isInWork) {
                book.setGoodreadsID(Integer.parseInt(sb.toString()));
            } // if-else
        } else if ("title".equals(qName)) {
            if (isInSeriesInner) {
                series.setName(sb.toString().trim());
            } else {
                book.setTitle(sb.toString());
            } // if-else
        } else if ("description".equals(qName)) {
            if (isInSeriesInner) {
                series.setDescription(sb.toString().trim());
            } else {
                book.setDescription(sb.toString());
            } // if-else
        } else if ("image_url".equals(qName)) {
            if (!isInAuthors) {
                book.setImageURL(sb.toString());
            } // if
        } else if ("language_code".equals(qName)) {
            book.setLanguage(sb.toString());
        } else if ("original_title".equals(qName)) {
            book.setOriginalTitle(sb.toString());
        } else if ("link".equals(qName)) {
            if (isInAuthors) {
                author.setUrl(sb.toString());
            } else {
                book.setUrl(sb.toString());
            } // if-else
        } else if ("name".equals(qName)) {
            if (isInAuthors) {
                author.setName(sb.toString());
            } // if
        } else if ("user_position".equals(qName)) {
            book.setNumberInSeries(Integer.parseInt(sb.toString()));
        } else if ("primary_work_count".equals(qName)) {
            series.setNumberOfBooks(Integer.parseInt(sb.toString()));
        } // if-else
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (newStartTag && !ignore) {
            sb.append(ch, start, length);
        } // if
    }

    @Override
    public void setDocumentLocator(Locator locator) {
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
    }
}
