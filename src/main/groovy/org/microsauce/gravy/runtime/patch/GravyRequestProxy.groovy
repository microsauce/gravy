package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic

import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.lang.patch.BaseEnterpriseProxy
import org.microsauce.gravy.module.Module

class GravyRequestProxy<T extends HttpServletRequest> extends BaseEnterpriseProxy {
	
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
		CommonObject obj = (CommonObject)((T)target).getAttribute(key)
		obj.value(context())
	}
	
	@CompileStatic void put(String key, Object value) {
		CommonObject obj = new CommonObject(value, context())
		((T)target).setAttribute key, obj
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

    @CompileStatic protected Module context() {
        module
    }
	
}

