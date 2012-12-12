package org.microsauce.gravy.context.javascript

import groovy.transform.CompileStatic

import java.lang.reflect.Proxy

import javax.script.CompiledScript;
import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.HandlerBinding
import org.microsauce.gravy.lang.coffeescript.CoffeeC;
import org.microsauce.gravy.lang.patch.BaseEnterpriseProxy
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeFunction
import org.mozilla.javascript.NativeJSON
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.ScriptableObject


// TODO put JSON attributes on the back burner

class JSHandler extends Handler {

	ScriptableObject scope 
	NativeObject jsObject
	
	JSHandler(NativeObject jsObject, ScriptableObject scope) {
		this.jsObject = jsObject
		this.scope = scope
	}
	
	@Override
	@CompileStatic
	public Object doExecute(HttpServletRequest req, HttpServletResponse res,
			FilterChain chain, HandlerBinding handlerBinding) {

		Context ctx = org.mozilla.javascript.Context.enter() 
		try {
			JSHttpSession jsSess = patchSession(req, ctx, scope)
			JSHttpServletRequest jsReq = patchRequest(req, res, jsSess, chain, ctx, scope)
			JSHttpServletResponse jsRes = patchResponse(req, res)
			jsObject.callMethod(
				jsObject, 
				'invokeHandler', 
				[jsReq, jsRes, handlerBinding.paramMap, handlerBinding.paramList] as Object[] 
			) 
		}
		finally {
			ctx.exit()
		}
	}
	
	@CompileStatic JSHttpServletRequest patchRequest(HttpServletRequest req, HttpServletResponse res, JSHttpSession sess, FilterChain chain, Context ctx, ScriptableObject scope) {
		JSHttpServletRequest jsReq = (JSHttpServletRequest) Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[JSHttpServletRequest.class] as Class[],
			new JSRequestProxy(req, res, sess, chain, ctx, scope))
		jsReq
	}		
	@CompileStatic JSHttpServletResponse patchResponse(HttpServletRequest req, HttpServletResponse res) {
		JSHttpServletResponse jsRes = (JSHttpServletResponse)Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[JSHttpServletResponse.class] as Class[],
			new JSResponseProxy(res, req, viewUri))
		return jsRes
	}
	@CompileStatic JSHttpSession patchSession(HttpServletRequest req, Context ctx, ScriptableObject scope) {
		JSHttpSession jsSess = (JSHttpSession)Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[JSHttpSession.class] as Class[],
			new JSSessionProxy(req.session, ctx, scope))
		return jsSess
	}

	@CompileStatic interface JSHttpServletRequest extends HttpServletRequest {
		Object get(String key)
		void put(String key, Object value)
		void doFilter()
		void forward(String uri)
		HttpSession session() // get the patched session
	}
	
	@CompileStatic interface JSHttpSession extends HttpSession {
		Object get(String key)
		void put(String key, Object value)
		void redirect(String url)
	}

	@CompileStatic interface JSHttpServletResponse extends HttpServletResponse {
		void render(String viewUri, Object model)
		void write(String output)
	}
	
	class JSResponseProxy<T extends HttpServletResponse> extends BaseEnterpriseProxy {
		
		HttpServletRequest request
		String viewUri
		
		JSResponseProxy(HttpServletResponse res, HttpServletRequest request, String viewUri) {
			super(res)
			this.request = request
			this.viewUri = viewUri
		}
		
		@CompileStatic void render(String _viewUri, Object model) {
			request.setAttribute('_view', _viewUri)
			request.setAttribute('_model', model) // TODO serialize as JSON first
			RequestDispatcher dispatcher = request.getRequestDispatcher(viewUri)
			dispatcher.forward(request, (T) target)
		}
		@CompileStatic void write(String output) {
			((T) target).writer.write(output)
		}
		@CompileStatic void redirect(String url) {
			((T) target).sendRedirect(url)
		}

	}
	
	class JSSessionProxy<T extends HttpSession> extends BaseEnterpriseProxy {
		
		Context ctx
		ScriptableObject scope
		
		JSSessionProxy(Object target, Context ctx, ScriptableObject scope) {
			super(target)
			this.ctx = ctx
			this.scope = scope
		}
		
		Object get(String key) {
			String jsonString = (String) ((T)target).getAttribute(key)
			NativeJSON.parse(ctx, scope, jsonString)
		}
		
		@CompileStatic 	void put(String key, Object value) {
			Object jsonValue = NativeJSON.stringify(ctx, scope, value, null, null)
			((T)target).setAttribute key, jsonValue
		}
		
	} 
	
	class JSRequestProxy<T extends HttpServletRequest> extends BaseEnterpriseProxy {
		
		Context ctx
		ScriptableObject scope
		FilterChain chain
		HttpServletResponse response
		HttpSession session
		
		JSRequestProxy(Object target, HttpServletResponse res, HttpSession session, FilterChain chain, Context ctx, ScriptableObject scope) {
			super(target)
			this.response = res
			this.session = session
			this.chain = chain
			this.ctx = ctx
			this.scope = scope
		}

		// @CompileStatic //  TODO
		Object get(String key) {
			String jsonString = (String) ((T)target).getAttribute(key)
			NativeJSON.parse(ctx, scope, jsonString)
		}
		
		@CompileStatic
		void put(String key, Object value) {
			Object jsonValue = NativeJSON.stringify(ctx, scope, value, null, null)
			((T)target).setAttribute key, jsonValue
		}
				
		@CompileStatic void doFilter() {
			chain.doFilter((T)target, response)
		}
		@CompileStatic void forward(String uri) {
			RequestDispatcher dispatcher = ((T)target).getRequestDispatcher(uri)
			dispatcher.forward((T)target, response)
		}
		@CompileStatic HttpSession session() {
		 	session
		} 

	}
	
}
