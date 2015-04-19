package de.uni_leipzig.wcmprak.books.wcmbookserver.serve;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by Erik on 15.04.2015.
 */
public class SPAFilter implements Filter {
    private final static Logger log = LoggerFactory.getLogger(SPAFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI().substring(req.getContextPath().length());
        log.debug("SPAFilter: path=\"{}\" [\"{}\"]", path, req.getRequestURI());

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            // don't filter OPTIONS ...
            chain.doFilter(request, response);
        } else if (path.startsWith(Utils.API_URI)) {
            // Ignore api requests ...
            chain.doFilter(request, response);
        } else if (path.startsWith(Utils.STATIC_FILES_URI)) {
            // Ignore normal file requests to static file servlet ...
            chain.doFilter(request, response);
        } else if (path.startsWith(Utils.STATIC_SCRIPTS_URI) ||
                path.startsWith(Utils.STATIC_STYLES_URI) ||
                path.startsWith(Utils.STATIC_FONTS_URI) ||
                path.startsWith(Utils.STATIC_IMAGES_URI)) {
            // Redirect static file requests to static file servlet
            request.getRequestDispatcher(Utils.STATIC_FILES_URI + path.substring(1)).forward(request, response);
        } else {
            log.debug("SPAFilter: redirect path=\"{}\" to \"{}\"", path, Utils.STATIC_FILE_SPA_INDEX_URI);
            // Redirect everything else to SPA index file
            request.getRequestDispatcher(Utils.STATIC_FILE_SPA_INDEX_URI).forward(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
