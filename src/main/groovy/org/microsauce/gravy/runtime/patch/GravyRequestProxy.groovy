package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic

import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import org.microsauce.gravy.lang.patch.BaseEnterpriseProxy
import org.microsauce.gravy.module.Module
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeFunction
import org.mozilla.javascript.NativeJSON
import org.mozilla.javascript.ScriptableObject

abstract class GravyRequestProxy<T extends HttpServletRequest> extends BaseEnterpriseProxy {
	
	FilterChain chain
	HttpServletResponse response
	HttpSession session
	Module module
	
	GravyRequestProxy(Object target, HttpServletResponse res, HttpSession session, FilterChain chain, Module module) {
		super(target)
		this.response = res
		this.session = session
		this.chain = chain
		this.module = module
	}

	@CompileStatic Object get(String key) {
		Object value = ((T)target).getAttribute(key)
		if ( module.serializeAttributes && value != null) {
			value = parse((String)value) 
		}
		value
	}
	
	@CompileStatic void put(String key, Object value) {
		Object attrValue = value
		if ( module.serializeAttributes )
			attrValue = stringify(value) 
		((T)target).setAttribute key, attrValue
	}
			
	@CompileStatic void next() {
		chain.doFilter((T)target, response)
	}
	@CompileStatic void forward(String uri) {
		RequestDispatcher dispatcher = ((T)target).getRequestDispatcher(uri)
		dispatcher.forward((T)target, response)
	}
	@CompileStatic HttpSession session() {
	 	session
	} 

	protected abstract String stringify(Object object)
	
	protected abstract Object parse(String serializedObject)

}

