package de.uni_leipzig.comprak.books.wcmbookserver.extract.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik on 02.01.2015.
 */
public class BookEditionsList {
    private int editionsID;
    private int mainBookGoodreadsID;
    private String mainBookTitle;
    private AuthorInfo mainAuthor;
    private List<BookEditionInfo> books = new ArrayList<>();

    @XmlElement
    public int getEditionsID() {
        return editionsID;
    }

    public void setEditionsID(int editionsID) {
        this.editionsID = editionsID;
    }

    @XmlElement
    public AuthorInfo getMainAuthor() {
        return mainAuthor;
    }

    public void setMainAuthor(AuthorInfo author) {
        this.mainAuthor = author;
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

    @XmlElement
    @XmlElementWrapper
    public List<BookEditionInfo> getBooks() {
        return books;
    }

    public void addBook(BookEditionInfo book) {
        if (book != null) {
            this.books.add(book);
        } // if
    }
}
