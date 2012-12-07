package org.microsauce.gravy.dev.observer

import static org.microsauce.gravy.util.PathUtil.*

import org.microsauce.gravy.*
import org.microsauce.gravy.app.script.*
import org.microsauce.gravy.lang.groovy.script.ModuleScriptDecorator
import org.microsauce.gravy.lang.groovy.script.Script
import org.microsauce.gravy.lang.groovy.script.ScriptUtils
import org.microsauce.gravy.module.Module
import groovy.transform.CompileStatic

class RedeploySourceModHandler implements SourceModHandler {
	
	Module app
	
	RedeploySourceModHandler(Module app) {
		this.app = app
	}

	@CompileStatic
	void handle() { 
		try {
			app.context.clearAppliationServices()	
			app.load()
//			app.reset()
//			Script script = new Script([name: 'app', sourceUri:"${config.appRoot}${SLASH}WEB-INF${SLASH}modules${SLASH}app${SLASH}application.groovy"])
//			new ModuleScriptDecorator(config, app).decorate(script)
//			ScriptUtils.run(script) 
//			app.complete()
		}
		catch(all) {
			all.printStackTrace()
		}
	}
}