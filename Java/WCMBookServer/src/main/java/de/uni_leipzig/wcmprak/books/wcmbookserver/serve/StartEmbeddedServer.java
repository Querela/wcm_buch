package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by Erik on 12.04.2015.
 */
public class StartEmbeddedServer {
    private final static Logger log = LoggerFactory.getLogger(StartEmbeddedServer.class);

    protected static ResourceConfig getResources() {
        return new ResourceConfig()
                .packages("de.uni_leipzig.wcmprak.books.wcmbookserver.serve.resources");
    }

    protected static ServletContextHandler createContext() {
        String webDir = StartEmbeddedServer.class.getClassLoader().getResource("webapp-static").toExternalForm();
        log.info("webdir = {}", webDir);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        //context.setResourceBase(webDir);

        // Add jersey
        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/api/*");
        jerseyServlet.setInitOrder(1);
        jerseyServlet.setInitParameter("jersey.config.server.provider.packages", "de.uni_leipzig.wcmprak.books.wcmbookserver.serve.resources");

        // Add static
        ServletHolder staticServlet = context.addServlet(DefaultServlet.class, "/static/*");
        staticServlet.setInitParameter("resourceBase", webDir);
        staticServlet.setInitParameter("pathInfoOnly", "true");
        // staticServlet.setInitParameter("dirAllowed", "false");

        return context;
    }

    protected static Server createServer() {
        // Build grizzly httpServer with jersey/jax-rs resources

        Server server = new Server(8080);

        server.setHandler(createContext());

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
        final Server server = createServer();

        try {
            // Start server
            server.start();
            log.info("--> Server listening on: {}", server.getURI().toASCIIString());

            // Register shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Stopping server ...");
                    try {
                        server.stop();
                    } catch (Exception e) {
                        log.error("server stop", e);
                    }
                }
            }, "shutdownHook-server"));

            // Wait for ENTER OR CTRL^C
            log.info("Press ENTER or CTRL^C to exit ...");
            System.in.read();

            // Stop server
            server.dumpStdErr();
            server.stop();
            server.join();
        } catch (IOException ioex) {
            log.error("IO Exception while configuring and starting the server ...", ioex);
        }
    }
}
