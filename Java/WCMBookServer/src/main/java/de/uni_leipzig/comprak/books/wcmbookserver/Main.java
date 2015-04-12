package de.uni_leipzig.comprak.books.wcmbookserver;

import de.uni_leipzig.comprak.books.wcmbookserver.serve.StartEmbeddedServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point of WCMBookServer.
 * Created by Erik on 12.04.2015.
 */
public class Main {
    /**
     * Entry point ...
     *
     * @param args Arguments
     */
    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(Main.class);
        log.info("Start WCMBookServer ...");

        StartEmbeddedServer.main(args);
    }
}
