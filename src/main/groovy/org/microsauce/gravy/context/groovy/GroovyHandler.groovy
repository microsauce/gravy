package org.microsauce.gravy.context.groovy

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

class GroovyHandler extends Handler {

	private Closure closure

	GroovyHandler(Closure closure) {
		this.closure = closure
	}

	@Override
	@CompileStatic public Object doExecute(HttpServletRequest req, HttpServletResponse res,
		FilterChain chain, HandlerBinding handlerBinding) {
		
		Object jsonObject = null
		if ( handlerBinding.json ) {
			CommonObject json = new CommonObject(null, GravyType.GROOVY)
			json.serializedRepresentation = handlerBinding.json
			jsonObject = json.toNative()
		}
		// patch the JEE runtime
		GravyHttpSession gSess = (GravyHttpSession)Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[GravyHttpSession.class] as Class[],
			new GroovySessionProxy(req.session, module)) 
		GravyHttpServletRequest gReq =  (GravyHttpServletRequest) Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[GravyHttpServletRequest.class] as Class[],
			new GroovyRequestProxy(req, res, gSess, chain, module))
		GravyHttpServletResponse gRes = (GravyHttpServletResponse)Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[GravyHttpServletResponse.class] as Class[],
			new GroovyResponseProxy(res, req, module.renderUri, module))
		
		// add the jee runtime to the closure binding
		Map binding = [:]
		binding.req = gReq
		binding.sess = gSess
		binding.res = gRes
		binding.out = res.writer
		binding.chain = chain
		binding.json = jsonObject
		if (req.method == 'GET') binding.query = req.parameterMap
		else if (req.method == 'POST' || req.method == 'PUT') binding.form = req.parameterMap		
		
		// add uri parameters 
		handlerBinding.paramMap.each { String key, String value ->
			binding[key] = value
		}
		String[] splat = handlerBinding.splat ?: []
		
		// add the splat
		binding.splat = splat
		
		Closure closure = (Closure) closure.clone()
		
		String[] _paramList = 
			closure.maximumNumberOfParameters == splat.size() ? 
			handlerBinding.paramList as String[] : [] as String[]
		
		closure.delegate = binding as Binding
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.call(_paramList.length == 1 ? _paramList[0] : _paramList)
	}

	@CompileStatic protected GravyType context() {
		GravyType.GROOVY
	}
		
	@CompileStatic public Object doExecute(Object params) {
		Closure closure = (Closure)closure.clone()
		closure.call(params)		
	}
	
	class GroovyResponseProxy<T extends HttpServletResponse> extends GravyResponseProxy {
		
		GroovyResponseProxy(HttpServletResponse res,
				HttpServletRequest request, String renderUri, Module module) {
			super(res, request, renderUri, module)
		}

		@CompileStatic GravyType context() { GravyType.GROOVY }
			
	}
	
	class GroovySessionProxy<T extends HttpSession> extends GravySessionProxy {
		
		public GroovySessionProxy(Object target, Module module) {
			super(target, module)
		}
		
		@CompileStatic GravyType context() { GravyType.GROOVY }
		
	}
	
	class GroovyRequestProxy<T extends HttpServletRequest> extends GravyRequestProxy {
		
		public GroovyRequestProxy(Object target, HttpServletResponse res,
				HttpSession session, FilterChain chain, Module module) {
			super(target, res, session, chain, module)
		}

		@CompileStatic GravyType context() { GravyType.GROOVY }

	}

}