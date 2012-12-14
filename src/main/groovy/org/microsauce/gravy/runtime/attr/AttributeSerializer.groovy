package org.microsauce.gravy.runtime.attr

import groovy.transform.CompileStatic

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

abstract class AttributeSerializer {
	
	boolean serializationEnabled
	def serializer
	
	Object get(String key, Object target) {
		Object value = getAttribute(key, target)
		if ( serializationEnabled )
			value = serializer.parse(value)	
			
		value
	}
	
	void put(String key, Object value, Object target) {
		String attrValue = value
		if ( serializationEnabled )
			attrValue = serializer.stringify(value)
			
		setAttribute key, attrValue, target
	}
	
	@CompileStatic private Object getAttribute(String key, Object target) {
		if ( target instanceof HttpServletRequest )
			return ((HttpServletRequest)target).getAttribute(key)
		else if ( target instanceof HttpSession )
			return ((HttpSession)target).getAttribute(key)
			
		throw new RuntimeException('invalid state')
	}
	
	@CompileStatic private void setAttribute(String key, Object value, Object target) {
		if ( target instanceof HttpServletRequest )
			((HttpServletRequest)target).setAttribute(key, value)
		else if ( target instanceof HttpSession )
			((HttpSession)target).setAttribute(key, value)
		else throw new RuntimeException('invalid state')
	}
}
