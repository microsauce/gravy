package org.microsauce.gravy.runtime;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
//    Map<String, EnterpriseService> paramPreconditions;

    RouteChain(ServletRequest req, ServletResponse res, FilterChain serverChain, List<EnterpriseService> routes, Map<String, EnterpriseService> paramPreconditions) {
        this.serverChain = serverChain;
        this.routes = routes;
//        this.paramPreconditions = paramPreconditions;

        EnterpriseService endPoint = endPoint();
        if ( endPoint != null )
            servletFacade = new ServletFacade((HttpServletRequest) req, (HttpServletResponse) res, this, endPoint.getUriPattern(), endPoint.getUriParamNames());
        else
            servletFacade = new ServletFacade((HttpServletRequest) req, (HttpServletResponse) res, this, null, null);

        for( String uriParam : servletFacade.getUriParamMap().keySet() ) {
System.out.println("uriParam: " + uriParam);
            EnterpriseService paramService = paramPreconditions.get(uriParam);
System.out.println("paramPreconditions: " + paramPreconditions);
System.out.println("paramService: " + paramService);
            if ( paramService != null ) {
System.out.println("adding route");
                routes.add(0, paramService);
            }
        }
    }

    public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        if (currentPosition >= routes.size())
            // finish up with the 'native' filter
            serverChain.doFilter(req, res);
        else {
System.out.println("currentPosition: " + currentPosition);
            EnterpriseService route = routes.get(currentPosition++);
            String method = ((HttpServletRequest) req).getMethod().toLowerCase();
System.out.println("method: " + method);
            Handler methodHandler = route.getHandlers().get(method);
System.out.println("methodHandler: " + methodHandler);

            Handler handler = methodHandler != null ? methodHandler : route.getHandlers().get(EnterpriseService.DEFAULT);
System.out.println("handler1: " + handler);
            handler = handler != null ? handler : route.getHandlers().get(method);
System.out.println("handler2: " + handler);

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

    private EnterpriseService endPoint() {
        EnterpriseService endPoint = routes.get(routes.size()-1);
        if ( endPoint.isEndPoint() ) return endPoint;
        else return null;
    }

}