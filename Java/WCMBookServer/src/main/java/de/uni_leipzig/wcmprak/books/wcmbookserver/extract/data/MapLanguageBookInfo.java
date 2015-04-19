package de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * Created by Erik on 19.04.2015.
 */
@XmlRootElement(name = "mapLangBook")
public class MapLanguageBookInfo {
    private int editionsID;
    private int mainBookGoodreadsID;
    private String mainBookTitle;
    private HashMap<String, Book> mapLanguageBook = new HashMap<>();

    public MapLanguageBookInfo() {

    }

    public MapLanguageBookInfo(BookEditionsList bookEditionsList) {
        this();

        this.setEditionsID(bookEditionsList.getEditionsID());
        this.setMainBookGoodreadsID(bookEditionsList.getMainBookGoodreadsID());
        this.setMainBookTitle(bookEditionsList.getMainBookTitle());
        for (Book book : bookEditionsList.getBooks()) {
            this.addBook(book);
        }
    }

    public static class MyHashMapEntryType {
        @XmlAttribute
        public String language;

        @XmlElement
        public Book book;

        public MyHashMapEntryType() {
        }

        public MyHashMapEntryType(Map.Entry<String, Book> e) {
            language = e.getKey();
            book = e.getValue();
        }
    }

    @XmlElement
    public int getEditionsID() {
        return editionsID;
    }

    public void setEditionsID(int editionsID) {
        this.editionsID = editionsID;
    }

    @XmlElement
    public String getMainBookTitle() {
        return mainBookTitle;
    }

    public void setMainBookTitle(String mainBookTitle) {
        this.mainBookTitle = mainBookTitle;
    }

    @XmlElement
    public int getMainBookGoodreadsID() {
        return mainBookGoodreadsID;
    }

    public void setMainBookGoodreadsID(int mainBookGoodreadsID) {
        this.mainBookGoodreadsID = mainBookGoodreadsID;
    }

    @XmlElementWrapper(name = "books")
    public List<MyHashMapEntryType> getBooks() {
        List<MyHashMapEntryType> books = new ArrayList<>();

        // Sort languages for output ...
        TreeMap<String, Book> tm = new TreeMap<>(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return ((String) o1).compareTo((String) o2);
            }
        });
        tm.putAll(mapLanguageBook);

        for (Map.Entry<String, Book> entry : tm.entrySet()) {
            books.add(new MyHashMapEntryType(entry));
        }

        return books;
    }

    public void addBook(Book book) {
        if (book != null) {
            String language = book.getLanguage();
            if (language == null) {
                return;
            }

            if (!this.mapLanguageBook.containsKey(language)) {
                this.mapLanguageBook.put(language, book);
            }
        } // if
    }
}
