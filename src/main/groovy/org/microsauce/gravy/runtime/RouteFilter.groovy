package org.microsauce.gravy.runtime

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

import org.microsauce.gravy.context.Context
import org.microsauce.gravy.context.EnterpriseService

@Log4j
class RouteFilter implements Filter {

	Context context
	String errorUri

	RouteFilter(Context context, String errorUri) {
		this.context = context
		this.errorUri = errorUri
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
				Error error = new Error(all)
				RequestDispatcher dispatcher = req.getRequestDispatcher(errorUri)
				dispatcher.forward(request, res)
			}
		}
	}

	void destroy(){}
	void init(javax.servlet.FilterConfig config){}

	@CompileStatic private FilterChain buildChain(FilterChain chain, ServletRequest req) {

		HttpServletRequest _req = (HttpServletRequest)req
		List<EnterpriseService> matchingRoutes = context.findService(
			getUri((HttpServletRequest)req), _req.dispatcherType)
		FilterChain routeChain = null
		if ( matchingRoutes.size() > 0 ) 
			routeChain = new RouteChain(chain, matchingRoutes)
		
		routeChain
	}
	
	@CompileStatic String getUri(HttpServletRequest req) {
		String uri
		if ( req.getContextPath() != '/' )
			uri = req.requestURI.substring(req.contextPath.length())
		else uri = req.requestURI

		uri
	}

}
