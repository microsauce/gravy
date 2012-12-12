package org.microsauce.gravy.context

import groovy.transform.CompileStatic

import java.util.regex.Pattern

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.module.Module
import org.microsauce.gravy.runtime.ErrorHandler

/**
 * A request handler defined in an application script.
 * 
 * @author jboone
 */
abstract class Handler {
	
	// TODO add module reference here
	Module module
	ErrorHandler errorHandler 
	String viewUri
	
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
			errorHandler.handleError(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
				"execution failed for uri ${req.getRequestURI()}", 
				req, res, t)
		}
	}
	
	// TODO is this too restricting (abstract) ???
	private void prepareRequest(HttpServletRequest req) {}
	
	private void prepareResponse(HttpServletResponse res) {
		res.contentType = 'text/html'
	}

}