package de.uni_leipzig.comprak.books.wcmbookserver.extract.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik on 06.12.2014.
 */
@XmlRootElement(name = "series")
public class SeriesInfo {
    private int goodreadsID;
    private String name;
    private String description;
    private int countSeries;
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
    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    @XmlElement
    public int getNumberOfBooks() {
        return countSeries;
    }

    public void setNumberOfBooks(int count) {
        this.countSeries = count;
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
