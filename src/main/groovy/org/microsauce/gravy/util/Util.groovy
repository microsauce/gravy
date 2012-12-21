package org.microsauce.gravy.util

import groovy.transform.CompileStatic;

/**
 * this class will be used primarily to provide IO functionality to JS
 * 
 * @author jboone
 *
 */
class Util {
	
	@CompileStatic String readFileAsString(String path) {
		new File(path).text
	}

}
