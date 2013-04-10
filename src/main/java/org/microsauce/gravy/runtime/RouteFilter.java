package org.microsauce.gravy.runtime;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.microsauce.gravy.context.Context;
import org.microsauce.gravy.context.EnterpriseService;

class RouteFilter implements Filter {

    Logger log = Logger.getLogger(RouteFilter.class);

    Context context;
    String errorUri;

    RouteFilter(Context context, String errorUri) {
        this.context = context;
        this.errorUri = errorUri;
    }

    public void doFilter(ServletRequest request, ServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        FilterChain routeChain = buildChain(chain, req, res);

        if (routeChain == null) {
            log.debug("no routes defined for uri " + req.getRequestURI());
            chain.doFilter(req, res);
        } else {
            try {
                routeChain.doFilter(req, res);
            } catch (Exception all) {
                all.printStackTrace();
                Error error = new Error(all);
                req.setAttribute("error", error);
                RequestDispatcher dispatcher = req.getRequestDispatcher(errorUri);
                dispatcher.forward(request, res);
            }
        }
    }

    public void destroy() {}

    public void init(javax.servlet.FilterConfig config) {}

    private FilterChain buildChain(FilterChain chain, ServletRequest req, ServletResponse res) {

        HttpServletRequest _req = (HttpServletRequest) req;
        List<EnterpriseService> matchingRoutes = context.findService(
                getUri((HttpServletRequest) req), _req.getDispatcherType());
        FilterChain routeChain = null;
        if (matchingRoutes.size() > 0)
            routeChain = new RouteChain(req, res, chain, matchingRoutes);

        return routeChain;
    }

    String getUri(HttpServletRequest req) {
        String uri;
        if (!"/".equals(req.getContextPath()))
            uri = req.getRequestURI().substring(req.getContextPath().length());
        else uri = req.getRequestURI();

        return uri;
    }
}
