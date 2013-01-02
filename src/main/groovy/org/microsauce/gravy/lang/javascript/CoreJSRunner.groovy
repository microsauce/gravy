package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic

import org.mozilla.javascript.ScriptableObject

class CoreJSRunner extends JSRunner {
	
	public CoreJSRunner(List<File> roots) {
		super(roots)
	}

	@CompileStatic String[] getCoreScripts() {
		['core.js']
	}
	
}
