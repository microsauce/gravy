package org.microsauce.gravy.context.javascript

import groovy.transform.CompileStatic

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.HandlerBinding
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeFunction
import org.mozilla.javascript.NativeJSON
import org.mozilla.javascript.ScriptableObject

class JSHandler extends Handler {

	ScriptableObject scope 
	NativeFunction jsFunction
	
	JSHandler(NativeFunction jsFunction, ScriptableObject scope) {
		this.jsFunction = jsFunction
		this.scope = scope
	}
	
	@Override
	@CompileStatic
	public Object doExecute(HttpServletRequest req, HttpServletResponse res,
			FilterChain chain, HandlerBinding handlerBinding) {

		Context ctx = org.mozilla.javascript.Context.enter() 
		try {
			// make the handler binding available to JS
			req.setAttribute('_handlerBinding', handlerBinding)
			JSHttpServletRequest jsReq = (JSHttpServletRequest)Proxy.newProxyInstance(
				this.class.getClassLoader(),  
				[JSHttpServletRequest.class] as Class[],
				new JSRequestProxy(req, ctx, scope));
			jsFunction.call(ctx, scope, null, [jsReq, res, chain] as Object[] ) // TODO the user defined JS function will have only two parameters (req, res) this function is itself wrapped in a three argument function
		}
		finally {
			ctx.exit()
		}
	}
			
	// TODO refactor to another name/package, useful outside of JS/CS
	//
	class JSResponse {
		// add the following:
		// render
	}
	
	interface JSHttpServletRequest extends HttpServletRequest {
		Object attr(String key, Object value)
	}
	
	class JSRequestProxy implements InvocationHandler {
		
		HttpServletRequest request
		Context ctx
		ScriptableObject scope
		
		JSRequestProxy(HttpServletRequest request, Context ctx, ScriptableObject scope) {
			this.request = request
			this.ctx = ctx
			this.scope = scope
		}
		
		@CompileStatic
		public Object invoke(final Object   proxy, final Method method,
			final Object[] args) throws Throwable {
			
			Object value = null
			if (method.name == 'attr')
				value = attr((String)args[0], args[1])
			else {
				Method targetMethod = request.class.getDeclaredMethod(method.name, method.parameterTypes)
				value = targetMethod.invoke(request, args)
			}

			return value
		}
		//@CompileStatic // when compile statically I don't have access to NativeJSON.pars(3 arg)
		Object attr(String key, Object value) {
			Object ret = value
			if ( value != null ) {
				Object jsonValue = NativeJSON.stringify(ctx, scope, value, null, null)
				request.setAttribute key, jsonValue
			}
			else {
				String jsonString = (String) request.getAttribute(key)
				ret = NativeJSON.parse(ctx, scope, jsonString)
			}
				
			return ret
		}
	}
	
}
