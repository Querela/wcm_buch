package de.uni_leipzig.wcmprak.books.wcmbookserver.serve.resources;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Erik on 14.04.2015.
 */
// @XmlAccessorType(XmlAccessType.FIELD) // --> for serializing fields (rename fields different to getter/setter)
@XmlRootElement
public class Data {

    private String field;

    public Data() {
        field = "???2";
    }

    public Data(String field) {
        this.field = field;
    }

    @XmlElement
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
