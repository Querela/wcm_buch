package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import org.glassfish.grizzly.http.server.CLStaticHttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.servlet.FilterRegistration;
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

/**
 * Created by Erik on 12.04.2015.
 */
public class StartEmbeddedServer {
    private final static Logger log = LoggerFactory.getLogger(StartEmbeddedServer.class);

    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = getBaseURI().toString();
    public static final String JERSEY_SERVLET_CONTEXT_PATH = "";

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

    protected static WebappContext getWebappContext() {
        WebappContext webappContext = new WebappContext("Grizzly WebappContext", JERSEY_SERVLET_CONTEXT_PATH);

        return webappContext;
    }

    protected static HttpServer createServer() {
        // Build grizzly httpServer with jersey/jax-rs resources
        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(getBaseURI(), getResources(), false);

        // Build webapp context for registering additional servlets
        WebappContext webappContext = getWebappContext();
        webappContext.deploy(server);

        /*
        try {
            server.getServerConfiguration()
                    .addHttpHandler(
                            new CLStaticHttpHandler(
                                    new URLClassLoader(
                                            new URL[]{new URL("file:///D/Documents/Universität/WCM-Praktikum/wcm_buch/Java/WCMBookServer/target/WCMBookServer-javadoc.jar")}
                                    )), "/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //*/

        return server;
    }

    /**
     * Entry point ...
     *
     * @param args Arguments
     */
    public static void main(String[] args) throws Exception {
        log.info("Start embedded server for [WCMBookServer] ...");

        // Build & configure server
        final HttpServer server = createServer();

        try {
            // Start server
            server.start();

            // Register shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Stopping server ...");
                    server.shutdown();
                }
            }, "shutdownHook-server"));

            // Wait for ENTER OR CTRL^C
            log.info("Press ENTER or CTRL^C to exit ...");
            System.in.read();
            // Stop server
            server.shutdown(3, TimeUnit.SECONDS);
        } catch (IOException ioex) {
            log.error("IO Exception while configuring and starting the server ...", ioex);
        }
    }
}
