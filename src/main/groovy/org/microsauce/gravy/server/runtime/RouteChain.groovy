package org.microsauce.gravy.server.runtime

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletResponse
import javax.servlet.ServletRequest
import javax.servlet.Filter
import javax.servlet.FilterChain

import org.microsauce.gravy.app.*
import org.microsauce.gravy.server.util.ServerUtils
import groovy.transform.CompileStatic

class RouteChain implements FilterChain {

	def routes
	def currentPosition = 0
	def serverChain

	RouteChain(serverChain, routes) {
		this.serverChain = serverChain
		this.routes = routes
	}

	void doFilter(ServletRequest req, ServletResponse res) {
		if (currentPosition >= routes.size())
			// finish up with the 'native' filter 
			serverChain.doFilter(req, res) // TODO not entirely sure this will work
		else {
			def route = routes[currentPosition++]
			bindAndRouteRequest(route, req, res)
		}
	}

	def bindAndRouteRequest(route, req, res) {
		def requestUri = req.requestURI

		def matches =  requestUri =~ route.uriPattern
		def binding = [:]
		def ndx = 1
		route.params.each { param ->
			binding[param] = matches[0][ndx++]
		}

		binding.route = route.uriPattern.toString()
		binding.controller = null
		binding.chain = this
		binding << ServerUtils.buildContext(req, res, route.binding)

		def action = route.action
//		action.delegate = binding as Binding
//		action.resolveStrategy = Closure.DELEGATE_FIRST
		ActionUtils.call(action, binding) //action.call()
		res.writer.flush()
	}
}