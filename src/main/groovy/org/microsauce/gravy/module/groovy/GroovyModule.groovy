package org.microsauce.gravy.module.groovy

import groovy.transform.CompileStatic

import org.microsauce.gravy.context.Context
import org.microsauce.gravy.lang.groovy.api.GroovyAPI
import org.microsauce.gravy.lang.groovy.script.Script
import org.microsauce.gravy.lang.groovy.script.ScriptDecorator
import org.microsauce.gravy.lang.groovy.script.ScriptUtils
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.util.monkeypatch.groovy.GravyDecorator

class GroovyModule extends Module { // TODO pull code in from ScriptUtils,  

	static {
		//
		// decorate - I can do this here because all modules have their own classloader
		//
		GravyDecorator.decorateBinding() // TODO refactor into module.groovy
		GravyDecorator.decorateHttpServletRequest() // TODO refactor into gravy module loader
	}
	
	@Override
	@CompileStatic
	protected Object doLoad(Map binding) {
		// TODO for now use the 'old' groovy script utilities
		ConfigObject root = new ConfigObject()
		binding.root = root
		binding.config = config
		
		// create, initialize, and execute the script
		Script script = new Script()
		script.binding.putAll(binding) 
//		script.config = config 			// TODO why is this necessary ??? 
		script.classLoader = classLoader
		script.name = name
		script.sourceUri = scriptFile.absolutePath
		if ( folder.exists() ) {
			script.roots << folder.absolutePath
			File scriptsFolder = new File(folder, '/scripts')
			if ( scriptsFolder.exists() )
				script.roots << folder.absolutePath+"/scripts"
		}
		addClosure script.binding
		GroovyAPI.module = this

		Object _return = ScriptUtils.run script
		
		_return 
	}

	void addClosure(Map binding) {
		binding.run = { name, scriptBinding = null ->
			def subScript = new Script(
				[sourceUri: name+'.groovy', binding: [config:config, app:app], roots: script.roots, classLoader: script.classLoader])
			new ScriptDecorator(config, context).decorate(subScript)
			ScriptUtils.run(subScript)
		}
	}

}
