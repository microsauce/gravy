package org.microsauce.gravy.context.javascript

import groovy.transform.CompileStatic

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.HandlerBinding
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeFunction
import org.mozilla.javascript.ScriptableObject

class JSHandler extends Handler {

	ScriptableObject scope // TODO verify that 'context' is bound to the scope prior to application.js execution
	NativeFunction jsFunction
	
	JSHandler(NativeFunction jsFunction, ScriptableObject scope) {
		this.jsFunction = jsFunction
		this.scope = scope
	}
	
	@Override
	@CompileStatic
	public Object doExecute(HttpServletRequest req, HttpServletResponse res,
			FilterChain chain, HandlerBinding handlerBinding) {
		def contextFactory  = new org.mozilla.javascript.ContextFactory()
		Context ctx = contextFactory.enter()
		try {
			// make the handler binding available to JS
			req.setAttribute('_handlerBinding', handlerBinding)
			jsFunction.call(ctx, scope, null, [req, res, chain] as Object[] ) // TODO the user defined JS function will have only two parameters (req, res) this function is itself wrapped in a three argument function
		}
		finally {
			ctx.exit()
		}
	}
	
}
