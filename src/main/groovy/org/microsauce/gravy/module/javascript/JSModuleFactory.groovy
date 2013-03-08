package org.microsauce.gravy.module.javascript

import groovy.transform.CompileStatic
import org.microsauce.gravy.module.ModuleFactory
import org.microsauce.gravy.lang.object.GravyType

class JSModuleFactory extends ModuleFactory {

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

}
