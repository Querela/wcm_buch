package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import de.uni_leipzig.wcmprak.books.wcmbookserver.extract.utils.Props;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.io.IOException;
import java.util.EnumSet;

/**
 * Created by Erik on 12.04.2015.
 */
public class StartEmbeddedServer {
    private final static Logger log = LoggerFactory.getLogger(StartEmbeddedServer.class);

    protected static ServletContextHandler createContext() {
        String webDir = ".";
        try {
            webDir = Thread.currentThread().getContextClassLoader().getResource("webapp-static").toExternalForm();
        } catch (Exception ex) {
            log.error("get webDir failed ...", ex);
        }
        log.debug("WEB_DIR = \"{}\"", webDir);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        // context.setResourceBase(webDir);

        // Filter requests to redirect to single web page
        context.addFilter(SPAFilter.class, Utils.WILDCARD_SERVLET_MAPPING, EnumSet.of(DispatcherType.REQUEST));

        // Add some filters ...
        // TODO: configure?
        FilterHolder dosFilter = context.addFilter(org.eclipse.jetty.servlets.DoSFilter.class, Utils.WILDCARD_SERVLET_MAPPING, EnumSet.of(DispatcherType.REQUEST));
        FilterHolder qosFilter = context.addFilter(org.eclipse.jetty.servlets.QoSFilter.class, Utils.WILDCARD_SERVLET_MAPPING, EnumSet.of(DispatcherType.REQUEST));
        FilterHolder gzipFilter = context.addFilter(org.eclipse.jetty.servlets.GzipFilter.class, Utils.WILDCARD_SERVLET_MAPPING, EnumSet.of(DispatcherType.REQUEST));

        // Add jersey
        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, Utils.API_URI_SERVLET_MAPPING);
        jerseyServlet.setInitOrder(1);
        jerseyServlet.setInitParameter("jersey.config.server.provider.packages", "de.uni_leipzig.wcmprak.books.wcmbookserver.serve.resources");

        // Add static
        ServletHolder staticServlet = context.addServlet(DefaultServlet.class, Utils.STATIC_FILES_URI_SERVLET_MAPPING);
        staticServlet.setInitParameter("resourceBase", webDir);
        staticServlet.setInitParameter("pathInfoOnly", "true");
        staticServlet.setInitParameter("dirAllowed", "false");

        return context;
    }

    protected static Server createServer() {

        Server server = new Server(8080);

        server.setDumpAfterStart(false);
        server.setDumpBeforeStop(true);
        server.setStopAtShutdown(true);

        // Handler Structure
        HandlerCollection handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        handlers.setHandlers(new Handler[]{contexts, new DefaultHandler()});
        server.setHandler(handlers);

        contexts.addHandler(createContext());

        // === jetty-requestlog.xml ===
        NCSARequestLog requestLog = new NCSARequestLog();
        requestLog.setFilename("requestlog_yyyy_mm_dd.request.log");
        requestLog.setFilenameDateFormat("yyyy_MM_dd");
        requestLog.setRetainDays(90);
        requestLog.setAppend(true);
        requestLog.setExtended(true);
        requestLog.setLogCookies(false);
        requestLog.setLogTimeZone("GMT");
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(requestLog);
        handlers.addHandler(requestLogHandler);

        // === jetty-stats.xml ===
        StatisticsHandler stats = new StatisticsHandler();
        stats.setHandler(server.getHandler());
        server.setHandler(stats);

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

        // Configure and initialize an instance of DataExtractor & DataCache
        Props props = new Props();
        props.setStringProp("goodreads.api.key", "RwUzZwkv94PCodD1lMF5g");
        props.setStringProp(DataExtractor.PROP_KEY_ES_HOST, "ERIK-UBUNTU:9200");
        DataCache.getInstance().configureWith(props);
        DataCache.getInstance().initialize();
        DataExtractor.getInstance().configureWith(props);
        DataExtractor.getInstance().initialize();

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
            // server.dumpStdErr();
            server.stop();
            server.join();

            DataCache.getInstance().stop();
        } catch (IOException ioex) {
            log.error("IO Exception while configuring and starting the server ...", ioex);
        }
    }
}
