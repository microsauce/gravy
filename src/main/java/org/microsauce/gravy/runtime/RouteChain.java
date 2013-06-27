package org.microsauce.gravy.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.microsauce.gravy.context.EnterpriseService;
import org.microsauce.gravy.context.Handler;
import org.microsauce.gravy.context.ServletFacade;
import org.microsauce.incognito.Incognito;

/**
 *
 */
class RouteChain implements FilterChain {

    private static int PARAMS = 0;
    private static int SPLAT = 1;
    
    List<Handler> route;
    Integer currentPosition = 0;
    FilterChain serverChain;
    ServletFacade servletFacade;
    Incognito incognito;

    RouteChain(ServletRequest req, ServletResponse res, FilterChain serverChain, 
    		Incognito incognito, List<Handler> route, Map<String, EnterpriseService> paramPreconditions) {
        this.serverChain = serverChain;
        this.route = route;
        this.incognito = incognito;

        // TODO build a uri parameter map for each handler
        
        // TODO 
        // 1. refactor uri parsing code out of ServletFacade and into RouteChain
        // 2. 
        
        EnterpriseService endPoint = endPoint();
        if ( endPoint != null )
            servletFacade = new ServletFacade(
                    (HttpServletRequest) req, (HttpServletResponse) res,
                    this, incognito, endPoint.getUriPattern(),
                    endPoint.getUriParamNames());
        else
            servletFacade = new ServletFacade(
                    (HttpServletRequest) req, (HttpServletResponse) res,
                    this, incognito, null, null);

        List<Handler> paramHandlers= new ArrayList<Handler>();
        // TODO for each handler
        for( String uriParam : servletFacade.getUriParamMap().keySet() ) { // TODO for each handler.service.uriParamNames
            EnterpriseService paramService = paramPreconditions.get(uriParam);
            if ( paramService != null ) {
                paramHandlers.add(paramService.getHandlers().get(EnterpriseService.MIDDLEWARE));
            }
        }
        if ( paramHandlers.size() > 0 )
            route.addAll(0, paramHandlers);
    }
    
    public void closeOutputStream() {
	servletFacade.close();
    }
    
    public void flushAll() {
	servletFacade.flushAll();
    }

    public void doFilter(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        if (currentPosition >= route.size())
            // finish up with the 'native' filter
            serverChain.doFilter(req, res);
        else {
            Handler handler = route.get(currentPosition++);

            if ( handler == null ) doFilter(req, res); // there may not be a 'default' handler for this route
            else {
                try {
                    req.setAttribute("_module", handler.getModule());
                    ((HttpServletRequest) req).getSession().setAttribute("_module", handler.getModule());    // TODO is session scope necessary ???

                    GravyThreadLocal.SCRIPT_CONTEXT.set(handler.getModule().getScriptContext());
                    handler.execute(servletFacade);
                } catch (Throwable t) {
                    t.printStackTrace();
                } 
//                finally {
//                    if (!res.isCommitted())
//                        res.getOutputStream().flush();  // TODO review the flush here - this will preclude any other service on the chain from writing response headers
//                }
            }
        }
    }

    private EnterpriseService endPoint() {
        EnterpriseService endPoint = route.get(route.size()-1).getService();

        if ( endPoint != null && endPoint.isEndPoint() ) return endPoint;
        else return null;
    }
    
    private List applyPattern(HttpServletRequest req, Pattern uriPattern, List<String> uriParamNames) {
	List tuple = new ArrayList();
        String requestUri = req.getRequestURI();
        List<String> splat = new ArrayList<String>();
        List<String> uriParamValues = new ArrayList<String>();
        Map<String,String> uriParamMap = new HashMap<String,String>();
        tuple.add(uriParamMap);
        tuple.add(splat);

        if ( uriPattern != null ) {
            Matcher matches = uriPattern.matcher(requestUri); //requestUri =~ uriPattern
            Integer ndx = 1;
            if (uriParamNames != null && uriParamNames.size() > 0) {
                while (matches.find()) {
                    Integer groupCount = matches.groupCount();
                    for (; ndx <= groupCount; ndx++) {
                        String thisParamName = uriParamNames.get(ndx - 1);
                        String paramValue = matches.group(ndx);
                        uriParamValues.add(paramValue);
                        if (thisParamName.equals('*'))
                            splat.add(paramValue);
                        else {
                            uriParamMap.put(thisParamName, paramValue);
                        }
                    }
                }
            } else if (matches.groupCount() > 0) {
                Integer groupCount = matches.groupCount();
                while (matches.find()) {
                    for (; ndx <= groupCount; ndx++) {
                        splat.add(matches.group(ndx));
                    }
                }
            }
        }
        
        return tuple;
    }
    

}