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
	
	Module module
	
	abstract Object doExecute(HttpServletRequest req, HttpServletResponse res, FilterChain chain, HandlerBinding handlerBinding)
	abstract Object doExecute(Object ... params)
	
	@CompileStatic
	Object execute(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Pattern uriPattern, List<String> params) {
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
	
	@CompileStatic Object execute(Object ... parms) {
		doExecute(parms)
	}
	
	@CompileStatic Object execute() {
		doExecute([] as Object[])
	}
	
}