package org.microsauce.gravy.module.javascript

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import org.microsauce.gravy.context.Context
import org.microsauce.gravy.lang.coffeescript.CoffeeC
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.util.Util
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.ScriptableObject


@Log4j
class JSModule extends Module {

	Util util = new Util()
	
	@Override
	@CompileStatic
	protected Object doLoad(Map<String, Object> binding) {
		
		// initialize the module context
		org.mozilla.javascript.Context ctx = org.mozilla.javascript.Context.enter() 
		
		try {
			// instantiate the module script context
			if (!scriptContext) scriptContext = new ImporterTopLevel(ctx) 
			
			ScriptableObject _scope = (ScriptableObject) scriptContext
			_scope.put('gravyModule', _scope, this)
			_scope.put('out', _scope, System.out)
			_scope.put('log', _scope, log)
			_scope.put('util', _scope, util)
			_scope.put('config', _scope, config.toProperties())

			// add module return values to the script scope (app only)
			if ( binding ) {
				binding.each { String key, Object value ->
					_scope.put(key, _scope, value)
				}
			}

			// read and evaluate gravy.js
			InputStream gsStream = this.class.classLoader.getResourceAsStream('gravy.js')
			InputStreamReader gsReader = new InputStreamReader(gsStream)
			ctx.evaluateReader(_scope, gsReader, "gravy.js", 1, null)
			
			// read and evaluate application.js
			String applicationScript = load(scriptFile.name, folder)
			if ( scriptFile.name.endsWith('.coffee') ) {
				println ''
				println '========================================================================='
				println '= compiled coffee script (application.coffee.js)                        ='
				println '========================================================================='
				println ''
				int lineNumber = 1
				applicationScript.eachLine { String line ->
					println "${lineNumber++}: $line"
				}
				println ''
			}			
			ctx.evaluateString(_scope, applicationScript, scriptFile.name, 1, null)

		}
		finally {
			ctx.exit()
		}
		
		context
	}
	
	@CompileStatic public String load(String scriptUri) {
		load(scriptUri, new File(this.folder, 'lib'))
	}
		
	
	@CompileStatic public String load(String scriptUri, File baseDir) {
		log.info "loading ${this.name} script $scriptUri"
		File scriptFile = new File(this.folder, scriptUri)
		if ( scriptFile.exists() ) {
			if ( scriptUri.endsWith('.js') ) {
				return scriptFile.text
			}
			else if ( scriptUri.endsWith('.coffee') ) {
				String script = null
				File compiledScriptFile = new File(scriptFile.absolutePath+'.js')
				if (scriptFile.lastModified() > compiledScriptFile.lastModified()) {
					log.info "compiling ${scriptFile.absolutePath} to ${scriptFile.absolutePath}.js"
					CoffeeC coffeec = new CoffeeC(this.class.classLoader)
					script = coffeec.compile scriptFile.text
					compiledScriptFile.write script
				} else
					script = compiledScriptFile.text
				
				return script
			}
		} else {
			log.warn "script uri $scriptUri not found in module lib ${this.folder.absolutePath}/lib"
			return null
		}
	}

	
}
