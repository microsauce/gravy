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
    Module module
    Map<String, Object> nativeRepresentations
    String serializedRepresentation

    Stringer stringer

    CommonObject(Object nativeValue, Module module) {
        this.module = module
        this.nativeType = module.type
        nativeRepresentations = new HashMap<String, Object>();
        nativeRepresentations.put(getKey(nativeType.type, module.name), nativeValue);
        this.stringer = Stringer.getInstance();
    }

    @CompileStatic
    Object value(Module destModule) {

        Object nativeObj = nativeRepresentations.get(getKey(destModule.type.type, destModule.name));
        if (nativeObj) {
            return nativeObj;
        } else if (serializedRepresentation) {
            return stringer.parse(serializedRepresentation, destModule.type);
        } else {
            nativeObj = stringer.parse(this.toString(), destModule.type);
            nativeRepresentations.put(getKey(destModule.type.type, destModule.name), nativeObj);
            return nativeObj;
        }
    }

    @CompileStatic
    String toString() {
        if (!serializedRepresentation) {
            serializedRepresentation = stringer.toString(
                    nativeRepresentations.get(getKey(nativeType.type, module.name)), nativeType)
        }

        serializedRepresentation
    }

    @CompileStatic
    Object toNative() {
        Object nativeObj = nativeRepresentations.get(getKey(nativeType.type, module.name))
        if (!nativeRepresentations.containsKey(getKey(nativeType.type, module.name))) {
            nativeObj = stringer.parse(serializedRepresentation, nativeType)
            nativeRepresentations.put(getKey(nativeType.type, module.name), nativeObj)
        }
        nativeObj
    }

    @CompileStatic private String getKey(String type, String modName) {
//        if ( type == GravyType.RUBY.type ) // jruby objects cannot be shared between jruby runtimes
//            return type +  module.name
//        else return type
        type
    }
}
