package org.microsauce.gravy.context

import groovy.transform.CompileStatic

import java.util.regex.Pattern

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.runtime.ErrorHandler

/**
 * A request handler defined in an application script.
 * 
 * @author jboone
 */
abstract class Handler {
	
	ErrorHandler errorHandler 
	
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
			errorHandler.handleError(500, "execution failed for uri ${req.getRequestURI()}", req, res, t)
		}
	}
	
	private void prepareRequest(HttpServletRequest req) {}
	
	private void prepareResponse(HttpServletResponse res) {
		res.contentType = 'text/html'
	}

}