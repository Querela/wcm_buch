package de.uni_leipzig.wcmprak.books.wcmbookserver;

import de.uni_leipzig.wcmprak.books.wcmbookserver.serve.StartEmbeddedServer;
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
    public static void main(String[] args) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.log.de.uni_leipzig.wcmprak.books.wcmbookserver", "debug"); // TODO: remove in final version

        Logger log = LoggerFactory.getLogger(Main.class);
        log.info("Start WCMBookServer ...");

        StartEmbeddedServer.main(args);
    }
}
