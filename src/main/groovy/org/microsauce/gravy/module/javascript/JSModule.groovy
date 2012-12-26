package org.microsauce.gravy.module.javascript

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import org.microsauce.gravy.lang.javascript.GravyJSRunner
import org.microsauce.gravy.lang.javascript.JSLoader
import org.microsauce.gravy.lang.javascript.JSRunner
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.util.Util


@Log4j
class JSModule extends Module {

	Util util
	JSLoader jsModuleLoader
	
	@Override
	@CompileStatic
	protected Object doLoad(Map<String, Object> binding) {
		
		Object returnValue = null
		JSRunner jsRunner = new GravyJSRunner([this.folder, new File(folder, '/scripts')] as List<File>, null)
		util = new Util(jsRunner)
		
		if (!scriptContext) scriptContext = jsRunner.scriptContext 
		Map<String, Object> jsBinding = [:]
		jsBinding.gravyModule = this
		jsBinding.out = System.out
		jsBinding.log = log
		jsBinding.util = util
		jsBinding.config = config.toProperties()
		jsBinding.exports = scriptContext

		// add module exports to the script scope (app only)
		if ( binding ) {
			binding.each { String key, Object value ->
				jsBinding[key] = value
			}
		}

		jsRunner.run(scriptFile.name, jsBinding)
	}
	
}
