package de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik on 02.01.2015.
 */
@XmlRootElement(name = "editions")
public class BookEditionsList {
    private int editionsID;
    private int mainBookGoodreadsID;
    private String mainBookTitle;
    private AuthorInfo mainAuthor;
    private List<Book> books = new ArrayList<>();

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

    @XmlElement(name = "book")
    @XmlElementWrapper(name = "books")
    public List<Book> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        if (book != null) {
            this.books.add(book);
        } // if
    }
}
