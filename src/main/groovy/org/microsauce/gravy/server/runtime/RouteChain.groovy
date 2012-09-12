package org.microsauce.gravy.server.runtime

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletResponse
import javax.servlet.ServletRequest
import javax.servlet.Filter
import javax.servlet.FilterChain
import java.util.regex.Matcher

import org.microsauce.gravy.app.*
import org.microsauce.gravy.server.util.ServerUtils
import groovy.transform.CompileStatic

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

	private void bindAndRouteRequest(Route route, ServletRequest req, ServletResponse res) {
		HttpServletRequest request = (HttpServletRequest)req		
		String requestUri = request.requestURI
		String method = request.method.toLowerCase()

		Matcher matches =  requestUri =~ route.uriPattern
		Map binding = [:]
		Integer ndx = 1
		route.params.each { param ->
			binding[param] = matches[0][ndx++]
		}

		binding.route = route.uriPattern.toString()
		binding.controller = null
		binding.chain = this
		binding << ServerUtils.buildContext(request, (HttpServletResponse)res, route.binding)

		Closure action = route.getAction(method)
		ActionUtils.call(action, binding) 
		res.writer.flush()
	}
}