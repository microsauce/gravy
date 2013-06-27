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
import org.microsauce.gravy.context.Handler;
import org.microsauce.incognito.Incognito;

class RouteFilter implements Filter {

    Logger log = Logger.getLogger(RouteFilter.class);

    Context context;
    String errorUri;
    Incognito incognito;

    RouteFilter(Context context, Incognito incognito, String errorUri) {
        this.context = context;
        this.incognito = incognito;
        this.errorUri = errorUri;
    }

    public void doFilter(ServletRequest request, ServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        HttpServletRequest req = (HttpServletRequest) request;
        RouteChain routeChain = buildChain(chain, req, res);

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
            } finally {
                if (!res.isCommitted())
                    routeChain.flushAll();
                    //res.getOutputStream().flush();  
            }
        }
    }

    public void destroy() {}

    public void init(javax.servlet.FilterConfig config) {}

    private RouteChain buildChain(FilterChain chain, ServletRequest req, ServletResponse res) {

        HttpServletRequest _req = (HttpServletRequest) req;
        List<Handler> routeHandlers = context.makeRoute(
                getUri((HttpServletRequest) req), _req.getMethod());
        RouteChain routeChain = null;
        if (routeHandlers.size() > 0)
            routeChain = new RouteChain(req, res, chain, incognito, routeHandlers, context.getParamServices());

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
