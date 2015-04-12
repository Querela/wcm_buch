package de.uni_leipzig.comprak.books.wcmbookserver.extract.utils;

import java.util.Properties;

/**
 * Interface for marking a module as configurable with standard java properties.
 * Created by Erik on 28.11.2014.
 */
public interface Configurable {
    /**
     * Configures the module with the given Properties object <i>props</i>.
     *
     * @param props Properties with configurations for the module.
     */
    public void configureWith(Properties props);
}
