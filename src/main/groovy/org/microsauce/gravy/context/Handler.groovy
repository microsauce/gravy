package org.microsauce.gravy.context

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import java.util.regex.Pattern

import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.module.Module

/**
 * A request handler defined in an application script.
 * 
 * @author jboone
 */
@Log4j
abstract class Handler {
	
	// TODO add module reference here
	Module module
//	String viewUri
	
	abstract Object doExecute(HttpServletRequest req, HttpServletResponse res, FilterChain chain, HandlerBinding handlerBinding)
	
	@CompileStatic
	Object execute(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Pattern uriPattern, List<String> params) {
		prepareRequest(req)
		prepareResponse(res)
		HandlerBinding handlerBinding = new HandlerBinding(req, res, uriPattern, params)
		try {
			doExecute(req, res, chain, handlerBinding)
		}
		catch ( Throwable t ) {
			org.microsauce.gravy.runtime.Error error = new org.microsauce.gravy.runtime.Error(t)
			log.error "${error.errorCode} - ${error.errorMessage}", t
			req.setAttribute("error", error)
			RequestDispatcher dispatcher = req.getRequestDispatcher(module.errorUri)
			dispatcher.forward(req, res)
		}
	}
	
	// TODO is this too restricting (abstract) ???
	private void prepareRequest(HttpServletRequest req) {}
	
	private void prepareResponse(HttpServletResponse res) {
		res.contentType = 'text/html'
	}

}