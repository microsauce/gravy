package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic

import javax.servlet.http.HttpSession

import org.microsauce.gravy.lang.patch.BaseEnterpriseProxy
import org.microsauce.gravy.module.Module
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeJSON
import org.mozilla.javascript.ScriptableObject

abstract class GravySessionProxy<T extends HttpSession> extends BaseEnterpriseProxy {
	
	Module module
	
	GravySessionProxy(Object target, Module module) {
		super(target)
		this.module = module
	}
	
	Object get(String key) {
		Object value = ((T)target).getAttribute(key)
		if ( module.serializeAttributes )
			value = parse(value) 
		value
	}
	
	@CompileStatic void put(String key, Object value) {
		Object attrValue = value
		if ( module.serializeAttributes )
			attrValue = stringify(value) 
		((T)target).setAttribute key, attrValue
	}
	
	protected abstract String stringify(Object object)
	
	protected abstract Object parse(String serializedObject)

} 

