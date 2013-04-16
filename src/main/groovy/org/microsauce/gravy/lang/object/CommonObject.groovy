package org.microsauce.gravy.lang.object

import groovy.transform.CompileStatic
import org.microsauce.gravy.module.Module


/**
 *
 * This class encapsulates an object utilized by any or all supported
 * language environments (Groovy and JavaScript currently).  
 *
 * @author microsauce
 */
class CommonObject {

    GravyType nativeType
    Map<String, Object> nativeRepresentations
    String serializedRepresentation

    boolean polyglot

    Stringer stringer

    @CompileStatic CommonObject(Object nativeValue, GravyType nativeType, boolean polyglot) {
        this.nativeType = nativeType
        nativeRepresentations = new HashMap<String, Object>();
        nativeRepresentations.put(nativeType.name, nativeValue);
        this.stringer = Stringer.getInstance();

        // capture attribute state immediately in polyglot context
        this.polyglot = polyglot
        if ( polyglot && nativeValue ) toString()
    }

    @CompileStatic
    Object value(GravyType destType) {
        Object nativeObj = nativeRepresentations.get(destType.name);
        if (nativeObj) {
            return nativeObj;
        } else if (serializedRepresentation) {
            return stringer.parse(serializedRepresentation, destType);
        } else {
            nativeObj = stringer.parse(this.toString(), destType);
            nativeRepresentations.put(destType.name, nativeObj);
            return nativeObj;
        }
    }

    @CompileStatic
    String toString() {
        if (!serializedRepresentation) {
            serializedRepresentation = stringer.toString(
                    nativeRepresentations.get(nativeType.name), nativeType)
        }
        serializedRepresentation
    }

    @CompileStatic
    Object toNative() {
        Object nativeObj = nativeRepresentations.get(nativeType.name)
        if (!nativeRepresentations.containsKey(nativeType.name)) {
            nativeObj = stringer.parse(serializedRepresentation, nativeType)
            nativeRepresentations.put(nativeType.name, nativeObj)
        }
        nativeObj
    }

}
