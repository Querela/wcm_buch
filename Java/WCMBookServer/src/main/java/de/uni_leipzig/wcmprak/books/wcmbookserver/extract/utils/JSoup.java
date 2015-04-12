package de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by Erik on 31.10.2014.
 */
public class JSoup {
    public static Element getSingleElement(Elements eles) {
        if (eles == null || eles.size() == 0) {
            return null;
        } else {
            return eles.get(0);
        } // if-else
    }

    public static String getElementValue(Elements eles) {
        return getElementValue(getSingleElement(eles));
    }

    public static String getElementValue(Element ele) {
        if (ele == null) {
            return null;
        } else {
            return ele.text();
        } // if-else
    }

    public static String getAttributeValue(Elements eles, String attribute) {
        return getAttributeValue(getSingleElement(eles), attribute);
    }

    public static String getAttributeValue(Element ele, String attribute) {
        if (ele == null) {
            return null;
        } else {
            return ele.attr(attribute);
        } // if-else
    }

    public static boolean hasElements(Elements eles) {
        if (eles == null || eles.size() <= 0) {
            return false;
        } else {
            return true;
        } // if-else
    }
}
