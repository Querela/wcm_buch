package de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik on 02.01.2015.
 */
public class BookEditionInfo {
    private int goodreadsID;
    private String url;
    private String imageUrl;
    private String title;
    private String language;
    private String publisher;
    private String publishingDate;
    private List<AuthorInfo> authors = new ArrayList<>();

    @XmlElement
    public int getGoodreadsID() {
        return goodreadsID;
    }

    public void setGoodreadsID(int id) {
        this.goodreadsID = id;
    }

    @XmlElement
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @XmlElement
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @XmlElement
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @XmlElement
    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    @XmlElement
    public String getPublishingDate() {
        return publishingDate;
    }

    public void setPublishingDate(String publishingDate) {
        this.publishingDate = publishingDate;
    }

    // TODO: change class type lookup to support generics?
    @XmlElement(name = "author")
    @XmlElementWrapper(name = "authors")
    public List<AuthorInfo> getAuthors() {
        return authors;
    }

    public void addAuthor(AuthorInfo author) {
        if (author != null) {
            this.authors.add(author);
        } // if
    }
}
