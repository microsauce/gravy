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
	
	Stringer stringer;
	
	CommonObject(Object nativeValue, GravyType nativeType) {
		this.nativeType = nativeType;
		nativeRepresentations = new HashMap<String, Object>();
		nativeRepresentations.put(nativeType.type, nativeValue);
		this.stringer = Stringer.getInstance();  
	}
	
//	CommonObject(String serializedRepresentation, GravyType nativeType) {
//		this.nativeType = nativeType;
//		nativeRepresentations = new HashMap<String, Object>();
//		this.stringer = Stringer.getInstance(); 
//		this.serializedRepresentation = serializedRepresentation
//	}
	
	@CompileStatic Object value(GravyType context) {
println "retrieve CommonObject value for context ${context.type} - native type ${nativeType.type}"		
		Object nativeObj = nativeRepresentations.get(context.type);
		if ( nativeObj ) {
println "value 2"			
			return nativeObj;
		}
		else if ( serializedRepresentation ) {
println "value 3: $serializedRepresentation"			
			return stringer.parse(serializedRepresentation, context);
		}
		else {
println "value 4"			
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
		
println "serializedRepresentation: $serializedRepresentation"			
		serializedRepresentation
	}
	
	@CompileStatic Object toNative() {
		Object nativeObj = nativeRepresentations.get(nativeType.type)
		if ( !nativeObj ) {
			nativeObj = stringer.parse(serializedRepresentation, nativeType)
			nativeRepresentations.put(nativeType.type, nativeObj)
		}
		
		nativeObj
	}
}
