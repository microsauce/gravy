package org.microsauce.gravy.context.javascript

import groovy.transform.CompileStatic

import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.HandlerFactory
import org.mozilla.javascript.NativeFunction
import org.mozilla.javascript.ScriptableObject

class JSHandlerFactory extends HandlerFactory {

    @Override
    @CompileStatic
    public Handler makeHandler(Object rawHandler, Object scriptContext) {
        new JSHandler((NativeFunction) rawHandler, (ScriptableObject) scriptContext)
    }

}
