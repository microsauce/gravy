package org.microsauce.gravy.runtime

import groovy.transform.CompileStatic

import java.util.regex.Matcher

import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.app.*
import org.microsauce.gravy.context.Context
import org.microsauce.gravy.context.EnterpriseService
import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.groovy.ActionUtils

class RouteChain implements FilterChain {

	List<EnterpriseService> routes
	Integer currentPosition = 0
	FilterChain serverChain
	Context context

	RouteChain(FilterChain serverChain, List<EnterpriseService> routes) {
		this.serverChain = serverChain
		this.routes = routes
	}

	@CompileStatic
	void doFilter(ServletRequest req, ServletResponse res) {
		if (currentPosition >= routes.size())
			// finish up with the 'native' filter 
			serverChain.doFilter(req, res) 
		else {
			EnterpriseService route = routes[currentPosition++]
			String method = ((HttpServletRequest)req).method.toLowerCase()
			Handler handler = route.handlers[method] ?: route.handlers[EnterpriseService.DEFAULT]
			try {
				handler.execute((HttpServletRequest)req, (HttpServletResponse)res, serverChain, route.uriPattern, route.params)
			}
catch(Throwable t) {
	t.printStackTrace()				
}
			finally {
				if ( !res.committed )
					res.writer.flush()
			}
		}
	}

}