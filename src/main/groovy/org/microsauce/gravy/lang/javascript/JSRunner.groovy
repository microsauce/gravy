package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import org.mozilla.javascript.Context
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.ScriptableObject

@Log4j
abstract class JSRunner {
	
//	Util util = new Util()
//	JSLoader jsLoader
//	ScriptableObject scriptContext
	List<File> roots
	JSLoader jsLoader
	ScriptableObject scriptContext
	boolean coreLoaded = false
	
	JSRunner(List<File> roots, ScriptableObject scriptContext) {
		this.roots = roots
		
		if ( !scriptContext ) {
			org.mozilla.javascript.Context ctx = null
			try {
				ctx = org.mozilla.javascript.Context.enter()
				this.scriptContext = new ImporterTopLevel(ctx)
			}
			finally {
				ctx.exit()
			}
		} else this.scriptContext = scriptContext
		
		jsLoader = new JSLoader(roots)
	}
	@CompileStatic Object run(String scriptUri, Map<String, Object> binding) {
		run scriptUri, binding, null
	}
	@CompileStatic Object run(String scriptUri, Map<String, Object> binding, ScriptableObject scope) {
		Object returnValue = null
		
		org.mozilla.javascript.Context ctx = org.mozilla.javascript.Context.enter()
		
		try {
			// instantiate the module script context
			ScriptableObject jsContext = null
			if ( !scope && !scriptContext ) {
				jsContext = new ImporterTopLevel(ctx)
				scriptContext = jsContext
			}
			else if ( scope ) jsContext = scope
			else jsContext = scriptContext
			
			if ( binding ) {
				binding.each { String key, Object value ->
					jsContext.put(key, jsContext, value)
				}
			}
			
			if ( !coreLoaded ) loadCoreScripts ctx, jsContext
						
			// read and evaluate application.js
			String applicationScript = jsLoader.load(scriptUri)
			returnValue = ctx.evaluateString(jsContext, applicationScript, scriptUri, 1, null)

		}
		finally {
			ctx.exit()
		}
		
		returnValue
	}
	
	abstract String[] getCoreScripts() 
	
	@CompileStatic String loadScripts(List scriptRoots) {
		for (thisRoot in scriptRoots) {
			jsLoader.load("")
		}
	}
	
	@CompileStatic private loadCoreScripts(Context ctx, ScriptableObject _scope) {
		getCoreScripts().each { String thisScript ->
println "uri: $thisScript roots: $roots"			
			InputStream gsStream = this.class.classLoader.getResourceAsStream(thisScript)
			InputStreamReader gsReader = new InputStreamReader(gsStream)
			ctx.evaluateReader(_scope, gsReader, thisScript, 1, null)
		}
		
		coreLoaded = true
	}
}
