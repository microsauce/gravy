package org.microsauce.gravy.lang.object

import groovy.transform.CompileStatic
import org.microsauce.gravy.module.Module


/**
 * 
 * This class encapsulates an object utilized by any or all supported
 * language environments (Groovy and JavaScript currently).  
 * 
 * @author jboone
 */
class CommonObject {

	GravyType nativeType
    Module module
	Map<String, Object> nativeRepresentations
	String serializedRepresentation

	Stringer stringer
	
	CommonObject(Object nativeValue, Module module) { //GravyType nativeType) {
		this.nativeType = module.type
		nativeRepresentations = new HashMap<String, Object>();
		nativeRepresentations.put(nativeType.type+module.name, nativeValue);
		this.stringer = Stringer.getInstance();  
	}
	
	@CompileStatic Object value(Module destModule) {

		Object nativeObj = nativeRepresentations.get(destModule.type.type+destModule.name);
		if ( nativeObj ) {
			return nativeObj;
		}
		else if ( serializedRepresentation ) {
			return stringer.parse(serializedRepresentation, destModule.type);
		}
		else {
			nativeObj = stringer.parse(this.toString(), destModule.type);
			nativeRepresentations.put(destModule.type.type+destModule.name, nativeObj);
			return nativeObj;
		}
	}
	
	@CompileStatic String toString() {
		if ( !serializedRepresentation ) {
			serializedRepresentation = stringer.toString(
				nativeRepresentations.get(nativeType.type), nativeType)
		}

		serializedRepresentation
	}
	
	@CompileStatic Object toNative() {
		Object nativeObj = nativeRepresentations.get(nativeType.type)
		if ( !nativeRepresentations.containsKey(nativeType.type) ) {
			nativeObj = stringer.parse(serializedRepresentation, nativeType)
			nativeRepresentations.put(nativeType.type, nativeObj)
		}
		nativeObj
	}
}
