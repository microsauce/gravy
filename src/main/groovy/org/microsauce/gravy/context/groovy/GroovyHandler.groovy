package org.microsauce.gravy.context.groovy

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic

import java.util.regex.Pattern
import java.lang.reflect.Proxy

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.HandlerBinding
import org.microsauce.gravy.json.GravyJsonSlurper
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.runtime.patch.GravyHttpServletRequest
import org.microsauce.gravy.runtime.patch.GravyHttpServletResponse
import org.microsauce.gravy.runtime.patch.GravyHttpSession
import org.microsauce.gravy.runtime.patch.GravyRequestProxy
import org.microsauce.gravy.runtime.patch.GravyResponseProxy
import org.microsauce.gravy.runtime.patch.GravySessionProxy

class GroovyHandler extends Handler {

	private Closure closure
	private Closure jsonReviver = { k, val ->
		def retValue = val
		if ( val instanceof String && val.length() >= 19) {
			def substr = val.substring(0,19)

			if ( substr ==~ datePattern ) {
				retValue = Date.parse("yyyy-MM-dd'T'HH:mm:ss", substr)
			}
		}
		retValue
	}

	GroovyHandler(Closure closure) {
		this.closure = closure
	}

	@Override
	@CompileStatic public Object doExecute(HttpServletRequest req, HttpServletResponse res,
		FilterChain chain, HandlerBinding handlerBinding) {
		
		Object jsonObject = null
		if ( handlerBinding.json ) jsonObject = new GravyJsonSlurper().parseText(handlerBinding.json, jsonReviver)
		
		// patch the JEE runtime
		GravyHttpSession gSess = (GravyHttpSession)Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[GravyHttpSession.class] as Class[],
			new GroovySessionProxy(req.session, module, jsonReviver)) 
		GravyHttpServletRequest gReq =  (GravyHttpServletRequest) Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[GravyHttpServletRequest.class] as Class[],
			new GroovyRequestProxy(req, res, gSess, chain, module, jsonReviver))
		GravyHttpServletResponse gRes = (GravyHttpServletResponse)Proxy.newProxyInstance(
			this.class.getClassLoader(),
			[GravyHttpServletResponse.class] as Class[],
			new GroovyResponseProxy(res, req, module.renderUri, module, jsonReviver))
		
		// add the jee runtime to the closure binding
		Map binding = [:]
		binding.req = gReq
		binding.sess = gSess
		binding.res = gRes
		binding.out = res.writer
		binding.chain = chain
		binding.json = jsonObject
		
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

	@CompileStatic public Object doExecute(Object ... params) {
		Closure closure = (Closure)closure.clone()
		closure.call(params)		
	}
	
	private Pattern datePattern = ~/[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}/
	
	class GroovyResponseProxy<T extends HttpServletResponse> extends GravyResponseProxy {
		
		Closure reviver
		
		GroovyResponseProxy(HttpServletResponse res,
				HttpServletRequest request, String renderUri, Module module, Closure reviver) {
			super(res, request, renderUri, module)
			this.reviver = reviver
		}

		@CompileStatic String stringify(Object object) {
			new JsonBuilder(object).toString()
		}
		
		@CompileStatic Object parse(String serializedObject) {
			new GravyJsonSlurper().parseText(serializedObject, reviver)
		}
			
	}
	
	class GroovySessionProxy<T extends HttpSession> extends GravySessionProxy {
		
		Closure reviver
		
		public GroovySessionProxy(Object target, Module module, Closure reviver) {
			super(target, module)
			this.reviver = reviver
		}

		String stringify(Object object) {
			new JsonBuilder(object).toString()
		}
		
		Object parse(String serializedObject) {
			new GravyJsonSlurper().parseText(serializedObject, reviver)
		}
		
	}
	
	class GroovyRequestProxy<T extends HttpServletRequest> extends GravyRequestProxy {
		
		Closure reviver
		
		public GroovyRequestProxy(Object target, HttpServletResponse res,
				HttpSession session, FilterChain chain, Module module, Closure reviver) {
			super(target, res, session, chain, module)
			this.reviver = reviver
		}

		String stringify(Object object) {
			new JsonBuilder(object).toString()
		}
		
		Object parse(String serializedObject) {
			new GravyJsonSlurper().parseText(serializedObject, reviver)
		}

	}

}