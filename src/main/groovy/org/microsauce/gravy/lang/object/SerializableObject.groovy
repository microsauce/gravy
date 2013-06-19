package org.microsauce.gravy.lang.object

import groovy.transform.CompileStatic
import org.microsauce.gravy.module.Module
import org.microsauce.incognito.Incognito
import org.microsauce.incognito.Runtime
import org.microsauce.gravy.lang.object.GravyType
import static org.microsauce.gravy.lang.object.GravyType.*


/**
 *
 * This class encapsulates an object defined by one of the Stringer
 * supported language runtimes (Rhino, Groovy, and JRuby currently) and provides
 * methods for JSON serialization / deserialization.
 *
 * @author microsauce
 */
class SerializableObject implements SharedObject {

    GravyType nativeType
    Map<String, Object> nativeRepresentations
    String serializedRepresentation

    Stringer stringer

    SerializableObject(Object nativeValue, GravyType nativeType) {
        this.nativeType = nativeType
        nativeRepresentations = new HashMap<String, Object>();
        nativeRepresentations.put(nativeType.name, nativeValue);
        this.stringer = Stringer.getInstance();
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
