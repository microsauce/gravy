package org.microsauce.gravy.runtime

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.context.Context
import org.microsauce.gravy.context.EnterpriseService
import org.microsauce.gravy.util.ServerUtils

@Log4j
class RouteFilter implements Filter {

	Context context
	ErrorHandler errorHandler

	RouteFilter(Context context, ErrorHandler errorHandler) {
		this.context = context
		this.errorHandler = errorHandler
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

		HttpServletRequest _req = (HttpServletRequest)req
		List<EnterpriseService> matchingRoutes = context.findService(
			ServerUtils.getUri((HttpServletRequest)req), _req.dispatcherType)
		FilterChain routeChain = null
		if (matchingRoutes.size() >0) 
			routeChain = new RouteChain(chain, matchingRoutes)
		
		routeChain
	}

}
