package org.microsauce.gravy.module.groovy

import groovy.transform.CompileStatic

import org.microsauce.gravy.context.Context
import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.groovy.GroovyHandler
import org.microsauce.gravy.lang.groovy.api.GroovyAPI
import org.microsauce.gravy.lang.groovy.script.Script
import org.microsauce.gravy.lang.groovy.script.ScriptDecorator
import org.microsauce.gravy.lang.groovy.script.ScriptUtils
import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.module.Module


class GroovyModule extends Module {   

	@CompileStatic protected Object doLoad(Map imports) {
		ConfigObject root = new ConfigObject()
		Map exp = [:]
		Map binding = [:]
		binding.root = root
		binding.config = config
		
		// create, initialize, and execute the script
		Script script = new Script()
		script.binding.putAll(prepareImports(imports)) 
		script.binding.putAll(binding)
		script.binding.exp = exp
		script.classLoader = classLoader
		script.name = name
		script.sourceUri = scriptFile.absolutePath
		if ( folder.exists() ) {
			script.roots << folder.absolutePath
			File scriptsFolder = new File(folder, '/lib')
			if ( scriptsFolder.exists() )
				script.roots << folder.absolutePath+"/lib"
		}
		addClosure script.binding
		GroovyAPI.module = this

		ScriptUtils.run script
		prepareExports exp
	}

	@CompileStatic Map<String, GroovyHandler> prepareExports(Map exports) {
		Map<String, GroovyHandler> preparedExports = new HashMap<String, GroovyHandler>()
		exports.each { String name, Closure export ->
			preparedExports[name] = new GroovyHandler(export)			
		}
		preparedExports
	}
	 
	@CompileStatic Map<String, Map<String, Closure>> prepareImports(Map<String, Map<String, Handler>> allImports) {
		Map<String, Map<String, Closure>> preparedImports = new HashMap<String, Map<String, Closure>>()
		allImports.each { String moduleName, Map<String, Handler> imports ->
			imports.each { String name, Handler handler ->
				if ( !preparedImports.get(moduleName) ) preparedImports.put(moduleName, new HashMap<String, Closure>())
				Closure closure = { Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7 ->
					handler.call(
						commonObj(p1),
						commonObj(p2),
						commonObj(p3),
						commonObj(p4),
						commonObj(p5),
						commonObj(p6),
						commonObj(p7))
				}
				preparedImports.get(moduleName).put(name, closure)
			}
		}
		
		preparedImports
	}
	
	private CommonObject commonObj(Object obj) {
		obj ? new CommonObject(obj, GravyType.GROOVY) : null
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
