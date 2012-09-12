package org.microsauce.gravy.server.runtime

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletResponse
import javax.servlet.ServletRequest
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import org.microsauce.gravy.server.util.ServerUtils

import org.microsauce.gravy.app.ApplicationContext;
import org.microsauce.gravy.app.Route;
import org.microsauce.gravy.server.util.ServerUtils
import org.microsauce.gravy.ErrorHandler
import groovy.util.logging.Log4j
import groovy.transform.CompileStatic

@Log4j
class RouteFilter implements Filter {

	ApplicationContext applicationContext
	ErrorHandler errorHandler

	RouteFilter() {
		applicationContext = ApplicationContext.instance
		errorHandler = ErrorHandler.getInstance()
	}

	@CompileStatic
	void doFilter(ServletRequest request, ServletResponse res, FilterChain chain)  {
		HttpServletRequest req = (HttpServletRequest) request
		FilterChain routeChain = buildChain chain, req
		if ( !routeChain ) {
			log.debug "no routes defined for uri ${req.requestURI}"
			chain.doFilter(req, res)
		}
		else {
			try {
				routeChain.doFilter(req, res)
			}
			catch (Exception all) {
				all.printStackTrace()
				errorHandler.handleError HttpServletResponse.SC_INTERNAL_SERVER_ERROR, all.message, req, res, all 
			}
		}
	}

	void destroy(){}
	void init(javax.servlet.FilterConfig config){}

	@CompileStatic
	private FilterChain buildChain(FilterChain chain, ServletRequest req) {

		List<Route> matchingRoutes = applicationContext.findRoutes(
			ServerUtils.getUri((HttpServletRequest)req), req.dispatcherType)
		FilterChain routeChain = null
		if (matchingRoutes.size() >0) 
			routeChain = new RouteChain(chain, matchingRoutes)
		
		routeChain
	}

}
