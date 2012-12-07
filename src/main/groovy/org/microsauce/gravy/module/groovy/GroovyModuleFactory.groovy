package org.microsauce.gravy.module.groovy

import groovy.transform.CompileStatic

import org.microsauce.gravy.module.ModuleFactory

class GroovyModuleFactory extends ModuleFactory {

	@Override
	@CompileStatic
	public String moduleClassName() {
		'org.microsauce.gravy.module.groovy.GroovyModule'
	}

	@Override
	@CompileStatic
	public String moduleScriptName() {
		'application.groovy'
	}

}
