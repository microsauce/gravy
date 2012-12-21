package org.microsauce.gravy.context.javascript

import groovy.transform.CompileStatic

import java.lang.reflect.Proxy

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.HandlerBinding
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.runtime.patch.GravyHttpServletRequest
import org.microsauce.gravy.runtime.patch.GravyHttpServletResponse
import org.microsauce.gravy.runtime.patch.GravyHttpSession
import org.microsauce.gravy.runtime.patch.GravyRequestProxy
import org.microsauce.gravy.runtime.patch.GravyResponseProxy
import org.microsauce.gravy.runtime.patch.GravySessionProxy
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeFunction
import org.mozilla.javascript.NativeJSON
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.ScriptableObject

class JSHandler extends Handler {

	ScriptableObject scope 
	NativeObject jsObject
	NativeFunction parseJson
	Context ctx
	
	JSHandler(NativeObject jsObject, ScriptableObject scope) {
		this.jsObject = jsObject
		this.scope = scope
		this.parseJson =  scope.get('parseJson', scope)
	}
	
	@Override
	@CompileStatic public Object doExecute(HttpServletRequest req, HttpServletResponse res,
			FilterChain chain, HandlerBinding handlerBinding) {

		ctx = org.mozilla.javascript.Context.enter() 
		try {
			Map<String, Object> objectBinding = null
			if ( handlerBinding.json ) { 
				objectBinding = [:] 
				objectBinding.json = parseJson.call(ctx, scope, scope, [handlerBinding.json] as Object[])

			}
			GravyHttpSession jsSess = patchSession(req, module) 
			GravyHttpServletRequest jsReq = patchRequest(req, res, jsSess, chain, module) 
			GravyHttpServletResponse jsRes = patchResponse(req, res, module)
			doExecute([jsReq, jsRes, handlerBinding.paramMap, handlerBinding.paramList, objectBinding] as Object[])
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
	
	@CompileStatic GravyHttpServletRequest patchRequest(HttpServletRequest req, HttpServletResponse res, GravyHttpSession sess, FilterChain chain, Module module) {
		GravyHttpServletRequest jsReq = (GravyHttpServletRequest) Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[GravyHttpServletRequest.class] as Class[],
			new JSRequestProxy(req, res, sess, chain, module, ctx, scope, parseJson))
		jsReq
	}
	@CompileStatic GravyHttpServletResponse patchResponse(HttpServletRequest req, HttpServletResponse res, Module module) {
		GravyHttpServletResponse jsRes = (GravyHttpServletResponse)Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[GravyHttpServletResponse.class] as Class[],
			new JSResponseProxy(res, req, module.renderUri, module, ctx, scope, parseJson))
		return jsRes
	}
	@CompileStatic GravyHttpSession patchSession(HttpServletRequest req, Module module) {
		GravyHttpSession jsSess = (GravyHttpSession)Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[GravyHttpSession.class] as Class[],
			new JSSessionProxy(req.session, module, ctx, scope, parseJson))
		return jsSess
	}

	class JSResponseProxy<T extends HttpServletResponse> extends GravyResponseProxy {

		Context ctx
		ScriptableObject scope
		NativeFunction parseJson
		
		JSResponseProxy(HttpServletResponse res,
				HttpServletRequest request, String renderUri, Module module,
				Context ctx, ScriptableObject scope, NativeFunction parseJson) {
			super(res, request, renderUri, module)
			this.ctx = ctx
			this.scope = scope
			this.parseJson = parseJson
		}

		@CompileStatic String stringify(Object object) {
			NativeJSON.stringify(ctx, scope, object, null, null)
		}
		
		@CompileStatic Object parse(String serializedObject) {
			parseJson.call(ctx, scope, scope, [serializedObject] as Object[])
		}

	}
	
	class JSSessionProxy<T extends HttpSession> extends GravySessionProxy {
		
		Context ctx
		ScriptableObject scope
		NativeFunction parseJson
		
		public JSSessionProxy(Object target, Module module, 
			Context ctx, ScriptableObject scope, NativeFunction parseJson) {
			super(target, module);
			this.ctx = ctx
			this.scope = scope
			this.parseJson = parseJson
		}

		@CompileStatic String stringify(Object object) {
			NativeJSON.stringify(ctx, scope, object, null, null)
		}
		
		@CompileStatic Object parse(String serializedObject) {
			parseJson.call(ctx, scope, scope, [serializedObject] as Object[])
		}
		
	} 
	
	class JSRequestProxy<T extends HttpServletRequest> extends GravyRequestProxy {
		
		Context ctx
		ScriptableObject scope
		NativeFunction parseJson
		
		public JSRequestProxy(Object target, HttpServletResponse res,
				HttpSession session, FilterChain chain, Module module, 
				Context ctx, ScriptableObject scope, NativeFunction parseJson) {
			super(target, res, session, chain, module)
			this.ctx = ctx
			this.scope = scope
			this.parseJson = parseJson
		}

		@CompileStatic String stringify(Object object) {
			NativeJSON.stringify(ctx, scope, object, null, null)
		}
		
		@CompileStatic Object parse(String serializedObject) {
			parseJson.call(ctx, scope, scope, [serializedObject] as Object[])
		}

	}
	
}
