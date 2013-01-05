package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import org.mozilla.javascript.Context
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject

@Log4j
abstract class JSRunner {
	
	List<File> roots
	JSLoader jsLoader
	Scriptable global
	
	JSRunner(List<File> roots) {
		this.roots = roots

		org.mozilla.javascript.Context ctx = null
		try {
			ctx = org.mozilla.javascript.Context.enter()
			global = new ImporterTopLevel(ctx)
			loadCoreScripts ctx, global
		}
		finally {
			ctx.exit()
		}
		
		jsLoader = new JSLoader(roots)
	}
	
	@CompileStatic Object require(String uri) {
		run(uri, null, true)
	}
	
	@CompileStatic Object run(String scriptUri, Map<String, Object> binding) {
		run scriptUri, binding, false
	}
	@CompileStatic Object run(String scriptUri, Map<String, Object> binding, boolean isRequire) {
		Object returnValue = null
		
		org.mozilla.javascript.Context ctx = org.mozilla.javascript.Context.enter()
		
		try {
			// instantiate the 'global' object
			Scriptable jsScope = null
			Scriptable exports = null
			if ( isRequire ) {
				Scriptable moduleScope = ctx.newObject(global)
				moduleScope.setParentScope(global)
				jsScope = moduleScope
			} else {
				jsScope = global
			}
			exports = ctx.newObject(global)
			
			if ( binding ) {
				binding.each { String key, Object value ->
					jsScope.put(key, jsScope, value)
				}
			}
			jsScope.put('exports', jsScope, exports)
			
			// read and evaluate application.js
			String applicationScript = jsLoader.load(scriptUri)
			ctx.evaluateString(jsScope, applicationScript, scriptUri, 1, null)
			returnValue = exports

		}
		finally {
			ctx.exit()
		}
		
		returnValue
	}
	
	abstract String[] getCoreScripts() 
	
	@CompileStatic private void loadCoreScripts(Context ctx, Scriptable _scope) {
		getCoreScripts().each { String thisScript ->
			InputStream gsStream = this.class.classLoader.getResourceAsStream(thisScript)
			InputStreamReader gsReader = new InputStreamReader(gsStream)
			ctx.evaluateReader(_scope, gsReader, thisScript, 1, null)
		}
	}
}
