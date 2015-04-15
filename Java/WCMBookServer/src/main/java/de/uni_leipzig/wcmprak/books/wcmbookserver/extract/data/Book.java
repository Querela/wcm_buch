package de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik on 01.12.2014.
 */
@XmlRootElement(name = "book")
public class Book {
    private int goodreadsID;
    private String title;
    private String description;
    private String url;
    private String imageURL;

    private String originalTitle;
    private String language;

    private float numberInSeries;
    private SeriesInfo series;

    private List<AuthorInfo> authors = new ArrayList<>();
    private List<Shelf> shelves = new ArrayList<>();

    private int editionsID;

    @XmlElement
    public int getGoodreadsID() {
        return goodreadsID;
    }

    public void setGoodreadsID(int id) {
        this.goodreadsID = id;
    }

    @XmlElement
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @XmlElement(name = "author")
    @XmlElementWrapper(name = "authors")
    // @XmlAnyElement
    public List<AuthorInfo> getAuthors() {
        return authors;
    }

    public void addAuthor(AuthorInfo author) {
        if (author != null) {
            this.authors.add(author);
        } // if
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    @XmlElement
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlElement
    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imgUrl) {
        this.imageURL = imgUrl;
    }

    @XmlElement
    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String origTitle) {
        this.originalTitle = origTitle;
    }

    @XmlElement
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String lang) {
        this.language = lang;
    }

    @XmlTransient
    public boolean isPartOfSeries() {
        return this.series != null;
    }

    @XmlElement
    public float getNumberInSeries() {
        return numberInSeries;
    }

    public void setNumberInSeries(float nmbr) {
        this.numberInSeries = nmbr;
    }

    @XmlElement
    public SeriesInfo getSeries() {
        return series;
    }

    public void setSeries(SeriesInfo series) {
        this.series = series;
    }

    @XmlElement(type = Shelf.class, name = "shelf")
    @XmlElementWrapper(name = "shelves")
    public List<Shelf> getShelves() {
        return shelves;
    }

    public void addShelf(Shelf shelf) {
        if (shelf != null) {
            this.shelves.add(shelf);
        } // if
    }

    @XmlElement
    public int getGoodreadsEditionsID() {
        return editionsID;
    }

    public void setGoodreadsEditionsID(int id) {
        this.editionsID = id;
    }
}
