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
			app.context.clearApplicationServices()
			app.load()
		}
		catch(all) {
			all.printStackTrace()
		}
	}
}