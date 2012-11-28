package org.microsauce.gravy.runtime

import groovy.transform.CompileStatic

import java.util.regex.Matcher

import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.app.*
import org.microsauce.gravy.context.ActionUtils
import org.microsauce.gravy.context.Route
import org.microsauce.gravy.util.ServerUtils

class RouteChain implements FilterChain {

	List<Route> routes
	Integer currentPosition = 0
	FilterChain serverChain

	RouteChain(serverChain, routes) {
		this.serverChain = serverChain
		this.routes = routes
	}

	@CompileStatic
	void doFilter(ServletRequest req, ServletResponse res) {
		if (currentPosition >= routes.size())
			// finish up with the 'native' filter 
			serverChain.doFilter(req, res) 
		else {
			Route route = routes[currentPosition++]
			bindAndRouteRequest(route, req, res)
		}
	}

	@CompileStatic
	private void bindAndRouteRequest(Route route, ServletRequest req, ServletResponse res) {
		HttpServletRequest request = (HttpServletRequest)req		
		String requestUri = request.requestURI
		String method = request.method.toLowerCase()

		Matcher matches =  requestUri =~ route.uriPattern
		Map binding = [:]
		Integer ndx = 1
		List<String> splat = []
		if ( route.params.size() > 0 ) {
//			route.params.each { param ->
//				if ( param == '*' )
//					splat << matches[0][ndx++]
//				else
//					binding[param] = matches[0][ndx++]
//			}
			while (matches.find()) {
				Integer groupCount = matches.groupCount()
				for (;ndx<=groupCount;ndx++) {
					String param = route.params[ndx-1]
					if ( param == '*' )
						splat << matches.group(ndx)
					else
						binding[param] = matches.group(ndx)
				}
			}
		} else if (matches.groupCount() > 0) {
			Integer groupCount = matches.groupCount()
			while (matches.find()) {
				for (;ndx <= groupCount; ndx++ ) {
					splat << matches.group(ndx)
				}
			}
		}
		binding['splat'] = splat

		binding.route = route.uriPattern.toString()
		binding.controller = null
		binding.chain = this
		binding << ServerUtils.buildContext(request, (HttpServletResponse)res, route.binding)

		Closure action = route.getAction(method)
		List<String> paramList = []
		if ( action.maximumNumberOfParameters == splat.size() ) 
			paramList = splat

		ActionUtils.call(action, binding, paramList) 
		res.writer.flush()
	}
}