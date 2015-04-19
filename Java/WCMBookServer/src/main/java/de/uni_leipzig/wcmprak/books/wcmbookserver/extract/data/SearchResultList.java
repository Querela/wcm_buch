package de.uni_leipzig.wcmprak.books.wcmbookserver.extract.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erik on 19.04.2015.
 */
@XmlRootElement(name = "search")
public class SearchResultList {
    private String searchTerm;
    private int resultsTotal = 0;
    private int resultsStart = 0;
    private int resultsEnd = 0;
    private int resultsPerPage = 0;
    private float timeToSearch = 0.f;
    private List<Book> books = new ArrayList<>();

    @XmlElement
    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    @XmlElement
    public int getResultsStart() {
        return resultsStart;
    }

    public void setResultsStart(int resultsStart) {
        this.resultsStart = resultsStart;
    }

    @XmlElement
    public int getResultsEnd() {
        return resultsEnd;
    }

    public void setResultsEnd(int resultsEnd) {
        this.resultsEnd = resultsEnd;
    }

    @XmlElement
    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public void setResultsPerPage(int resultsPerPage) {
        this.resultsPerPage = resultsPerPage;
    }

    @XmlElement
    public int getResultsTotal() {
        return resultsTotal;
    }

    public void setResultsTotal(int resultsTotal) {
        this.resultsTotal = resultsTotal;
    }

    @XmlElement
    public float getTimeToSearch() {
        return timeToSearch;
    }

    public void setTimeToSearch(float timeToSearch) {
        this.timeToSearch = timeToSearch;
    }

    @XmlElement(name = "book")
    @XmlElementWrapper(name = "books", nillable = true)
    public List<Book> getBooks() {
        return books;
    }

    public void addBook(Book book) {
        if (book != null) {
            this.books.add(book);
        } // if
    }
}
