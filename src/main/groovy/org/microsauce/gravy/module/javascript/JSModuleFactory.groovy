package org.microsauce.gravy.module.javascript

import groovy.transform.CompileStatic;

import java.io.File;

import org.microsauce.gravy.module.Module;
import org.microsauce.gravy.module.ModuleFactory

class JSModuleFactory extends ModuleFactory {

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


}
