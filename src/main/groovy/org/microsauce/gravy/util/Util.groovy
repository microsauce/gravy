package org.microsauce.gravy.util

import groovy.transform.CompileStatic;

/**
 * this class will be used primarily to bring some grooviness to the JS
 * 
 * @author jboone
 *
 */
class Util {
	
	@CompileStatic String readFileAsString(String path) {
		new File(path).text
	}

}
