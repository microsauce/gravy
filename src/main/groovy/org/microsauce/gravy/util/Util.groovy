package org.microsauce.gravy.util

import groovy.transform.CompileStatic

import org.microsauce.gravy.lang.javascript.JSLoader
import org.microsauce.gravy.lang.javascript.JSRunner

/**
 * this class will be used primarily to provide IO functionality to JS
 * 
 * @author jboone
 *
 */
class Util {
	
	JSRunner runner
	
	Util(JSRunner runner) {
		this.runner = runner
	}
	
	@CompileStatic public Object load(String scriptUri) {
		runner.run(scriptUri, null)
	}
	
	@CompileStatic public Object require(String scriptUri) {
		runner.require(scriptUri)
	}
	
	@CompileStatic String readFileAsString(String path) {
		new File(path).text
	}

}
