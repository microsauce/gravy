package org.microsauce.gravy.lang.object

import groovy.transform.CompileStatic
import org.microsauce.incognito.Incognito
import org.microsauce.incognito.Runtime
import static org.microsauce.gravy.lang.object.GravyType.*

/**
 *
 * This class encapsulates an object defined by one of the Incognito
 * supported language runtimes (Rhino, Groovy, and JRuby currently) and provides
 * methods for returning Incognito proxy wrappers.
 *
 * @author microsauce
 */

class ProxyObject implements SharedObject {
	
	static Map<String, Runtime.ID> typeMap = [:] 
	
	static {
		typeMap[GROOVY.name] = Runtime.ID.GROOVY
		typeMap[JAVASCRIPT.name] = Runtime.ID.RHINO
		typeMap[RUBY.name] = Runtime.ID.JRUBY
	}
	
	GravyType gravyType
	Incognito incognito
	Map <GravyType, Object> proxyMap = [:]
	
	@CompileStatic ProxyObject(GravyType gravyType, Object nativeObj, Incognito incognito) {
		
		this.gravyType = gravyType
		this.incognito = incognito
		proxyMap[gravyType] = nativeObj
	}
	
	@CompileStatic public Object value(GravyType destType) {
		Runtime.ID rtId = typeMap[destType.name]
		incognito.proxy(rtId, proxyMap[gravyType])
	}

	@CompileStatic public Object toNative() {
		proxyMap[gravyType.name]
	}
	
}
