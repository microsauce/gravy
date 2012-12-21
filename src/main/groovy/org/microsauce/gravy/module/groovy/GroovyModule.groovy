package org.microsauce.gravy.module.groovy

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic

import java.util.regex.Pattern

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import org.microsauce.gravy.context.Context
import org.microsauce.gravy.json.GravyJsonSlurper
import org.microsauce.gravy.lang.groovy.api.GroovyAPI
import org.microsauce.gravy.lang.groovy.script.Script
import org.microsauce.gravy.lang.groovy.script.ScriptDecorator
import org.microsauce.gravy.lang.groovy.script.ScriptUtils
import org.microsauce.gravy.module.Module

class GroovyModule extends Module {   

	@CompileStatic protected Object doLoad(Map binding) {
		ConfigObject root = new ConfigObject()
		binding.root = root
		binding.config = config
		
		// create, initialize, and execute the script
		Script script = new Script()
		script.binding.putAll(binding) 
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

		ScriptUtils.run script
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
