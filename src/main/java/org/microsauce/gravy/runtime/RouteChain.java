package org.microsauce.gravy.runtime;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.microsauce.gravy.context.Context;
import org.microsauce.gravy.context.EnterpriseService;
import org.microsauce.gravy.context.ServletFacade;
import org.microsauce.gravy.context.Handler;

/**
 *
 */
class RouteChain implements FilterChain {

    List<EnterpriseService> routes;
    Integer currentPosition = 0;
    FilterChain serverChain;
    ServletFacade servletFacade;

    RouteChain(ServletRequest req, ServletResponse res, FilterChain serverChain, List<EnterpriseService> routes) {
        this.serverChain = serverChain;
        this.routes = routes;

        EnterpriseService endPoint = endPoint();
        if ( endPoint != null )
            servletFacade = servletFacade((HttpServletRequest) req, (HttpServletResponse) res, this, endPoint.getUriPattern(), endPoint.getUriParamNames());
        else
            servletFacade = servletFacade((HttpServletRequest) req, (HttpServletResponse) res, this, null, null);
    }

    public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        if (currentPosition >= routes.size())
            // finish up with the 'native' filter
            serverChain.doFilter(req, res);
        else {
            EnterpriseService route = routes.get(currentPosition++);
            String method = ((HttpServletRequest) req).getMethod().toLowerCase();
            Handler methodHandler = route.getHandlers().get(method);

            Handler handler = methodHandler != null ? methodHandler : route.getHandlers().get(EnterpriseService.DEFAULT);
            handler = handler != null ? handler : route.getHandlers().get(method);

            if ( handler == null ) doFilter(req, res); // there may not be a 'default' handler for this route
            else {

                try {
                    ////
                    //
                    ////
                    req.setAttribute("_module", handler.getModule());
                    ((HttpServletRequest) req).getSession().setAttribute("_module", handler.getModule());    // TODO is session scope necessary ???

                    GravyThreadLocal.SCRIPT_CONTEXT.set(handler.getModule().getScriptContext());
                    handler.execute(servletFacade);
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    if (!res.isCommitted())
                        res.getOutputStream().flush();  // TODO review the flush here - this will preclude any other service on the chain from writing response headers
                }
            }
        }
    }

    private ServletFacade servletFacade(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Pattern uriPattern, List<String> params) {
//        ServletFacade wrapper = (ServletFacade)req.getAttribute("_wrapper");
//        if ( wrapper == null ) {
        ServletFacade wrapper = new ServletFacade(req, res, chain, uriPattern, params);
//            req.setAttribute("_wrapper", wrapper);
//        }
        return wrapper;
    }

    private EnterpriseService endPoint() {
        EnterpriseService endPoint = routes.get(routes.size()-1);
        if ( endPoint.isEndPoint() ) return endPoint;
        else return null;
    }

}