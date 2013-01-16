package org.microsauce.gravy.context.javascript

import groovy.transform.CompileStatic

import java.lang.reflect.Proxy

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.HandlerBinding
import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.runtime.patch.GravyHttpServletRequest
import org.microsauce.gravy.runtime.patch.GravyHttpServletResponse
import org.microsauce.gravy.runtime.patch.GravyHttpSession
import org.microsauce.gravy.runtime.patch.GravyRequestProxy
import org.microsauce.gravy.runtime.patch.GravyResponseProxy
import org.microsauce.gravy.runtime.patch.GravySessionProxy
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeFunction
import org.mozilla.javascript.NativeObject
import org.mozilla.javascript.ScriptableObject

/**
 * 
 * @author microsuace
 *
 */
class JSHandler extends Handler {

	ScriptableObject scope 
	NativeFunction callBack
	
	NativeFunction executeHandler
	Context ctx
	
	JSHandler(NativeFunction callBack, ScriptableObject scope) {
		this.callBack = callBack
		this.scope = scope
		this.executeHandler = scope.get('executeHandler', scope)
	}
	
	@Override
	@CompileStatic public Object doExecute(Object params) {
		ctx = org.mozilla.javascript.Context.enter()
		ctx.setLanguageVersion(Context.VERSION_1_8)
		try {
			return callBack.call(ctx, scope, scope, params as Object[])
		}
		finally {
			ctx.exit()
		}
	}

	@Override
	@CompileStatic public Object doExecute(HttpServletRequest req, HttpServletResponse res,
			FilterChain chain, HandlerBinding handlerBinding) {

		ctx = org.mozilla.javascript.Context.enter() 
		try {
			Map<String, Object> objectBinding = null
			if ( handlerBinding.json ) { 
				objectBinding = [:] 
				CommonObject json = new CommonObject(null, GravyType.JAVASCRIPT)
				json.serializedRepresentation = handlerBinding.json
				objectBinding.json = json.toNative()
			}
			GravyHttpSession jsSess = patchSession(req, module) 
			GravyHttpServletRequest jsReq = patchRequest(req, res, jsSess, chain, module) 
			GravyHttpServletResponse jsRes = patchResponse(req, res, module)
			executeHandler.call(ctx, scope, scope, [callBack, jsReq, jsRes, handlerBinding.paramMap, handlerBinding.paramList, objectBinding] as Object[])
		}
		finally {
			ctx.exit()
		}
	}
	
	@CompileStatic protected GravyType context() {
		GravyType.JAVASCRIPT
	}
			
	@CompileStatic GravyHttpServletRequest patchRequest(HttpServletRequest req, HttpServletResponse res, GravyHttpSession sess, FilterChain chain, Module module) {
		GravyHttpServletRequest jsReq = (GravyHttpServletRequest) Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[GravyHttpServletRequest.class] as Class[],
			new JSRequestProxy(req, res, sess, chain, module))
		jsReq
	}
	@CompileStatic GravyHttpServletResponse patchResponse(HttpServletRequest req, HttpServletResponse res, Module module) {
		GravyHttpServletResponse jsRes = (GravyHttpServletResponse)Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[GravyHttpServletResponse.class] as Class[],
			new JSResponseProxy(res, req, module.renderUri, module))
		return jsRes
	}
	@CompileStatic GravyHttpSession patchSession(HttpServletRequest req, Module module) {
		GravyHttpSession jsSess = (GravyHttpSession)Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[GravyHttpSession.class] as Class[],
			new JSSessionProxy(req.session, module))
		return jsSess
	}

	class JSResponseProxy<T extends HttpServletResponse> extends GravyResponseProxy {

		JSResponseProxy(HttpServletResponse res,
				HttpServletRequest request, String renderUri, Module module) {
			super(res, request, renderUri, module)
		}
				
		@CompileStatic GravyType context() {
			GravyType.JAVASCRIPT
		}
	}
	
	class JSSessionProxy<T extends HttpSession> extends GravySessionProxy {
		
		public JSSessionProxy(Object target, Module module) {
			super(target, module);
		}

		@CompileStatic GravyType context() {
			GravyType.JAVASCRIPT
		}

	} 
	
	class JSRequestProxy<T extends HttpServletRequest> extends GravyRequestProxy {
		
		public JSRequestProxy(Object target, HttpServletResponse res,
				HttpSession session, FilterChain chain, Module module) {
			super(target, res, session, chain, module)
		}
				
		@CompileStatic GravyType context() {
			GravyType.JAVASCRIPT
		}
	}
	
}
