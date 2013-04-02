package org.microsauce.gravy.module.javascript

import groovy.transform.CompileStatic
import org.microsauce.gravy.context.Context
import org.microsauce.gravy.lang.javascript.GravyJSRuntime
import org.microsauce.gravy.lang.javascript.JSRuntime
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.module.ModuleFactory
import org.microsauce.gravy.lang.object.GravyType

class JSModuleFactory extends ModuleFactory {

    JSRuntime runtime = new GravyJSRuntime([])

    @Override
    @CompileStatic
    public GravyType type() {
        GravyType.JAVASCRIPT
    }

    @Override
    @CompileStatic
    public String moduleClassName() {
        'org.microsauce.gravy.module.javascript.JSModule'
    }

    @Override
    @CompileStatic
    public String moduleScriptName() {
        'application.js'
    }

    @CompileStatic void initializeRuntime(Module module) {
        JSModule jsModule = module as JSModule
        jsModule.jsRuntime = runtime
    }

}
