package de.uni_leipzig.comprak.books.wcmbookserver.extract.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik on 06.12.2014.
 */
@XmlRootElement(name = "author")
public class AuthorInfo {
    private int goodreadsID;
    private String name;
    private String url;
    private String role;
    private List<Book> books = new ArrayList<>();

    @XmlElement
    public int getGoodreadsID() {
        return goodreadsID;
    }

    public void setGoodreadsID(int id) {
        this.goodreadsID = id;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @XmlElement
    @XmlElementWrapper
    public List<Book> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        if (book != null) {
            this.books.add(book);
        } // if
    }

}
