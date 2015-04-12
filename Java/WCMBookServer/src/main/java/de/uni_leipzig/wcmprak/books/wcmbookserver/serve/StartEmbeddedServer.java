package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Created by Erik on 12.04.2015.
 */
public class StartEmbeddedServer {
    private final static Logger log = LoggerFactory.getLogger(StartEmbeddedServer.class);

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://127.0.0.1:8080/wcmbook";

    protected static URI getBaseURI() {
        return UriBuilder
                .fromUri("http://localhost/")
                .port(8080)
                .path("wcmbook")
                .build();
    }

    protected static ResourceConfig getResources() {
        return new ResourceConfig()
                .packages("de.uni_leipzig.wcmprak.books.wcmbookserver.serve.resources");
    }

    protected static HttpServer createServer() {
        // Build grizzly httpServer with jersey/jax-rs resources
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(getBaseURI(), getResources(), false);

        return server;
    }

    /**
     * Entry point ...
     *
     * @param args Arguments
     */
    public static void main(String[] args) throws Exception {
        log.info("Start embedded server for [WCMBookServer] ...");

        final HttpServer server = createServer();

        try {
            server.start();

            // register shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Stopping server ...");
                    server.shutdown();
                }
            }, "shutdownHook-server"));

            log.info("Press ENTER or CTRL^C to exit ...");
            System.in.read();
            server.shutdown(3, TimeUnit.SECONDS);
        } catch (IOException ioex) {
            log.error("IO Exception while configuring and starting the server ...", ioex);
        }
    }
}
