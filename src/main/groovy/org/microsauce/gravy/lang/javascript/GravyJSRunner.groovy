package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic

import org.mozilla.javascript.ScriptableObject

class GravyJSRunner extends JSRunner {
	
	public GravyJSRunner(List<File> roots) {
		super(roots)
	}

	@CompileStatic String[] getCoreScripts() {
		['core.js','gravy.js']
	} 

}
