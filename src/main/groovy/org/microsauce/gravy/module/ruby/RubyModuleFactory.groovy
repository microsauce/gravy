
package org.microsauce.gravy.module.ruby

import groovy.transform.CompileStatic
import org.microsauce.gravy.context.ServletFacade
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.lang.ruby.RubyRuntime
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.module.ModuleFactory

import java.util.regex.Matcher
import java.util.regex.Pattern

class RubyModuleFactory extends ModuleFactory {

    RubyRuntime runtime

    @CompileStatic RubyModuleFactory() {
        super()
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
	if ( !runtime ) {
	    runtime = new RubyRuntime(rubyLoadPaths(System.getProperty('gravy.moduleRoot')))
	    ServletFacade.rubyContext = runtime.container
	}
        RubyModule rubyModule = module as RubyModule
        rubyModule.runtime = runtime
    }

    /*
         FIXME: i have discovered that the JRuby load path cannot be modified following the execution
         of the 'gravy.rb' script.  this method is a temporary workaround.  it scans the module root folder
         and builds a complete load path for which to initialize the RubyRuntime
     */
    @CompileStatic List<String> rubyLoadPaths(String modRoot) {
        List<String> rubyLoadPaths = []
        Pattern appFilePattern = ~/application\.([a-zA-Z0-9]+)/
        new File(modRoot).eachFile { File modFolder ->
            modFolder.eachFile { File thisFile ->
                if (thisFile.name ==~ appFilePattern) {
                    Matcher matcher = thisFile.name =~ appFilePattern
                    matcher.find()
                    String fileExtension = matcher.group(1)
                    File libFolder = new File(modFolder, 'lib')
                    if ( fileExtension == 'rb' && libFolder.exists()) {
                        if ( modFolder.name == 'app' )
                            rubyLoadPaths.add(0, libFolder.absolutePath)
                        else rubyLoadPaths << libFolder.absolutePath
                    }
                }
            }
        }
        rubyLoadPaths
    }
}

