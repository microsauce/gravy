package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.mozilla.javascript.ScriptableObject

class GravyJSRuntime extends JSRuntime {
	
	public GravyJSRuntime(List<File> roots, Logger logger) {
		super(roots, logger)
	}

	@CompileStatic String[] getCoreScripts() {
		['coffee-module-loader.js','core.js','gravy.js']
	} 

}
