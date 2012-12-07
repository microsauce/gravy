package org.microsauce.gravy.module.javascript

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import org.microsauce.gravy.context.Context
import org.microsauce.gravy.module.Module
import org.mozilla.javascript.ContextFactory
import org.mozilla.javascript.ImporterTopLevel
import org.mozilla.javascript.ScriptableObject

@Log4j
class JSModule extends Module {

	@Override
	@CompileStatic
	protected Object doLoad(Map<String, Object> binding) {
		
		// initialize the module context
//		Context context = new Context()
		ContextFactory contextFactory  = new org.mozilla.javascript.ContextFactory()
		org.mozilla.javascript.Context ctx = contextFactory.enter()
		
		try {
			// bind the module context and templateEngine
			if (!scriptContext) scriptContext = new ImporterTopLevel(ctx) 
			
			ScriptableObject _scope = (ScriptableObject) scriptContext
			
//			scope.put('context', scope, context) // TODO
			_scope.put('module', _scope, this)
			if ( binding ) {
				binding.each { String key, Object value ->
					_scope.put(key, null, value)
				}
			}
//			scope.put('templateEngine', scope, module.templateEngine)

			// read and evaluate gravy.js
			InputStream gsStream = this.class.classLoader.getResourceAsStream('gravy.js')
			InputStreamReader gsReader = new InputStreamReader(gsStream)
			ctx.evaluateReader(_scope, gsReader, "gravy.js", -1, null)
			
			// read and evaluate application.js
			File applicationScript = new File(folder, '/application.js')
			Reader applicationReader = new FileReader(applicationScript)
			ctx.evaluateReader(_scope, applicationReader, "application.js", -1, null)

			// assign the module to each route
			for ( service in context.enterpriseServices ) {
				service.module = this
			}
			
			for ( service in context.cronServices ) {
				service.module = this
			}			
		}
		catch ( all ) {
			log.error "failed to load module: ${name}", all
			all.printStackTrace()
			throw all
		}
		finally {
			ctx.exit()
		}
		
		context
	}
	
}
