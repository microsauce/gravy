package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic

import org.mozilla.javascript.ScriptableObject

class TestJSRunner extends JSRunner {
	
	public TestJSRunner(List<File> roots, ScriptableObject scope) {
		super(roots, scope)
	}

	@CompileStatic String[] getCoreScripts() {
		['core.js']
	}
	
}
