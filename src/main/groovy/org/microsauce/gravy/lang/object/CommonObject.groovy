package org.microsauce.gravy.lang.object

import groovy.transform.CompileStatic


/**
 * 
 * This class encapsulates an object utilized by any or all supported
 * language environments (Groovy and JavaScript currently).  
 * 
 * @author jboone
 */
class CommonObject {

	GravyType nativeType;
	Map<String, Object> nativeRepresentations;
	String serializedRepresentation;
	
	Stringer stringer
	
	CommonObject(Object nativeValue, GravyType nativeType) {
		this.nativeType = nativeType;
		nativeRepresentations = new HashMap<String, Object>();
		nativeRepresentations.put(nativeType.type, nativeValue);
		this.stringer = Stringer.getInstance();  
		if (nativeType.type == GravyType.RUBY.type) toString()
	}
	
	@CompileStatic Object value(GravyType context) {

		if ( context.type == GravyType.RUBY.type )
			return stringer.parse(serializedRepresentation, context);
		
		Object nativeObj = nativeRepresentations.get(context.type);
		if ( nativeObj ) {
			return nativeObj;
		}
		else if ( serializedRepresentation ) {
			return stringer.parse(serializedRepresentation, context);
		}
		else {
			nativeObj = stringer.parse(this.toString(), context);
			nativeRepresentations.put(context.type, nativeObj);
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
