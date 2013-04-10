package org.microsauce.gravy.module.javascript

import groovy.transform.CompileStatic
import org.microsauce.gravy.context.ServletFacade
import org.microsauce.gravy.lang.javascript.GravyJSRuntime
import org.microsauce.gravy.lang.javascript.JSRuntime
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.module.ModuleFactory
import org.microsauce.gravy.lang.object.GravyType

class JSModuleFactory extends ModuleFactory {

    JSRuntime runtime

    JSModuleFactory() {
        super()
        // TODO this will clutter the load path when app is not a JS module
        File appFolder = new File(new File(System.getProperty('gravy.moduleRoot')), 'app')
        File appLibFolder =  new File(appFolder, 'lib')
        List<File> libs = [appFolder]
        if (appLibFolder.exists()) libs.add(appLibFolder)
        runtime = new GravyJSRuntime(libs)
        // register the JS runtime with the ServletWrapper
        ServletFacade.jsContext = runtime.global
    }

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
