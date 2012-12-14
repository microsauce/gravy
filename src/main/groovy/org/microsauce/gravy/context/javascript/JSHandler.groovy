package org.microsauce.gravy.context.javascript

import groovy.transform.CompileStatic

import java.lang.reflect.Proxy

import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.HandlerBinding
import org.microsauce.gravy.lang.patch.BaseEnterpriseProxy
import org.microsauce.gravy.module.Module
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeJSON
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.ScriptableObject

class JSHandler extends Handler {

	ScriptableObject scope 
	NativeObject jsObject
	
	JSHandler(NativeObject jsObject, ScriptableObject scope) {
		this.jsObject = jsObject
		this.scope = scope
	}
	
	@Override
	@CompileStatic public Object doExecute(HttpServletRequest req, HttpServletResponse res,
			FilterChain chain, HandlerBinding handlerBinding) {

		Context ctx = org.mozilla.javascript.Context.enter() 
		try {
			JSHttpSession jsSess = patchSession(req, ctx, scope, module)
			JSHttpServletRequest jsReq = patchRequest(req, res, jsSess, chain, ctx, scope, module)
			JSHttpServletResponse jsRes = patchResponse(req, res, ctx, scope, module)
			doExecute([jsReq, jsRes, handlerBinding.paramMap, handlerBinding.paramList] as Object[])
		}
		finally {
			ctx.exit()
		}
	}
			
	@CompileStatic public Object doExecute(Object ... params) {
		jsObject.callMethod(
			jsObject, 'invokeHandler', params
		)

	}
	
	@CompileStatic JSHttpServletRequest patchRequest(HttpServletRequest req, HttpServletResponse res, JSHttpSession sess, FilterChain chain, Context ctx, ScriptableObject scope, Module module) {
		JSHttpServletRequest jsReq = (JSHttpServletRequest) Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[JSHttpServletRequest.class] as Class[],
			new JSRequestProxy(req, res, sess, chain, ctx, scope, module))
		jsReq
	}		
	@CompileStatic JSHttpServletResponse patchResponse(HttpServletRequest req, HttpServletResponse res, Context ctx, ScriptableObject scope, Module module) {
		JSHttpServletResponse jsRes = (JSHttpServletResponse)Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[JSHttpServletResponse.class] as Class[],
			new JSResponseProxy(res, req, module.renderUri, ctx, scope, module))
		return jsRes
	}
	@CompileStatic JSHttpSession patchSession(HttpServletRequest req, Context ctx, ScriptableObject scope, Module module) {
		JSHttpSession jsSess = (JSHttpSession)Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[JSHttpSession.class] as Class[],
			new JSSessionProxy(req.session, ctx, scope, module))
		return jsSess
	}

	@CompileStatic interface JSHttpServletRequest extends HttpServletRequest {
		Object get(String key)
		void put(String key, Object value)
		void doFilter()
		void forward(String uri)
		JSHttpSession session() // get the patched session
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
		String renderUri
		Context ctx
		ScriptableObject scope
		Module module
		
		JSResponseProxy(HttpServletResponse res, HttpServletRequest request, String renderUri, Context ctx, ScriptableObject scope, Module module) {
			super(res)
			this.request = request
			this.renderUri = renderUri
			this.ctx = ctx
			this.scope = scope
			this.module = module
		}
		
		@CompileStatic void render(String _viewUri, Object model) {
			request.setAttribute('_view', _viewUri)
			Object attrModel = model
			if ( module.serializeAttributes ) {
				attrModel = NativeJSON.stringify(ctx, scope, model, null, null)
			}
			request.setAttribute('_model', attrModel) 
			request.setAttribute('_module', module)
			RequestDispatcher dispatcher = request.getRequestDispatcher(renderUri)
			dispatcher.forward(request, (T) target)
		}
		@CompileStatic void write(String output) {
			((T) target).writer.write(output)
		}
		@CompileStatic void redirect(String url) {
			((T) target).sendRedirect(url)
		}
		@CompileStatic void renderJson(Object model) {
			((T) target).contentType = 'application/json'
			((T) target).writer << NativeJSON.stringify(ctx, scope, model, null, null)
			((T) target).writer.flush()
		}

	}
	
	class JSSessionProxy<T extends HttpSession> extends BaseEnterpriseProxy {
		
		Context ctx
		ScriptableObject scope
		Module module
		
		JSSessionProxy(Object target, Context ctx, ScriptableObject scope, Module module) {
			super(target)
			this.ctx = ctx
			this.scope = scope
			this.module = module
		}
		
		Object get(String key) {
			Object value = ((T)target).getAttribute(key)
			if ( module.serializeAttributes )
				value = NativeJSON.parse(ctx, scope, value)
			value
		}
		
		@CompileStatic void put(String key, Object value) {
			Object attrValue = value
			if ( module.serializeAttributes )
				attrValue = NativeJSON.stringify(ctx, scope, value, null, null)
			((T)target).setAttribute key, attrValue
		}
		
	} 
	
	class JSRequestProxy<T extends HttpServletRequest> extends BaseEnterpriseProxy {
		
		Context ctx
		ScriptableObject scope
		FilterChain chain
		HttpServletResponse response
		HttpSession session
		Module module
		
		JSRequestProxy(Object target, HttpServletResponse res, HttpSession session, FilterChain chain, Context ctx, ScriptableObject scope, Module module) {
			super(target)
			this.response = res
			this.session = session
			this.chain = chain
			this.ctx = ctx
			this.scope = scope
			this.module = module
		}

		Object get(String key) {
			Object value = ((T)target).getAttribute(key)
			if ( module.serializeAttributes )
				value = NativeJSON.parse(ctx, scope, value)
			value
		}
		
		@CompileStatic void put(String key, Object value) {
			Object attrValue = value
			if ( module.serializeAttributes )
				attrValue = NativeJSON.stringify(ctx, scope, value, null, null)
			((T)target).setAttribute key, attrValue
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
