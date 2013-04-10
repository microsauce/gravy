package org.microsauce.gravy.module.ruby

import groovy.transform.CompileStatic
import org.microsauce.gravy.context.ServletFacade
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.lang.ruby.RubyRuntime
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.module.ModuleFactory

class RubyModuleFactory extends ModuleFactory {

    RubyRuntime runtime

    @CompileStatic RubyModuleFactory() {
        super()
        // TODO this will clutter the load path when app is not a Ruby module
        File appFolder = new File(new File(System.getProperty('gravy.moduleRoot')), 'app')
        File appLibFolder =  new File(appFolder, 'lib')
        List<String> libs = [appFolder.absolutePath]
        if (appLibFolder.exists()) libs.add(appLibFolder.absolutePath)
        runtime = new RubyRuntime(libs)

        ServletFacade.rubyContext = runtime.container
    }

    @Override
    @CompileStatic
    public GravyType type() {
        GravyType.RUBY
    }

    @Override
    @CompileStatic
    public String moduleClassName() {
        'org.microsauce.gravy.module.ruby.RubyModule'
    }

    @Override
    @CompileStatic
    public String moduleScriptName() {
        'application.rb'
    }

    @CompileStatic void initializeRuntime(Module module) {
        RubyModule rubyModule = module as RubyModule
        rubyModule.runtime = runtime
    }


}

