package org.microsauce.gravy.context.groovy

import groovy.transform.CompileStatic

import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.context.EnterpriseService
import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.HandlerBinding

class GroovyHandler extends Handler {

	private Closure actionClosure

	GroovyHandler(Closure closure) {
		this.actionClosure = closure
	}

	@Override
	@CompileStatic
	public Object doExecute(HttpServletRequest req, HttpServletResponse res,
		FilterChain chain, HandlerBinding handlerBinding) {
		Map binding = handlerBinding.binding
		handlerBinding.paramMap.each { String key, String value ->
			binding[key] = value
		}
		String[] splat = handlerBinding.splat ?: []
		
		Closure closure = (Closure) actionClosure.clone()
		
		String[] _paramList = 
			closure.maximumNumberOfParameters == splat.size() ? 
			handlerBinding.paramList as String[] : [] as String[]
		
		closure.delegate = binding as Binding
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		closure.call(_paramList.length == 1 ? _paramList[0] : _paramList)
	}

	@CompileStatic public Object doExecute(Object ... params) {
		Closure closure = (Closure)actionClosure.clone()
		closure.call(params)		
	}
		
}