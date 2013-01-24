package org.microsauce.gravy.module.javascript

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.lang.javascript.GravyJSRunner
import org.microsauce.gravy.lang.javascript.JSRunner
import org.microsauce.gravy.lang.javascript.JSSerializer;
import org.microsauce.gravy.module.Module
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeFunction
import org.mozilla.javascript.ScriptableObject


@Log4j
class JSModule extends Module {

//	JSLoader jsModuleLoader
	JSRunner jsRunner

	@Override
	@CompileStatic
	protected Object doLoad(Map<String, Handler> imports) {

		jsRunner = new GravyJSRunner([this.folder, new File(folder, '/lib')] as List<File>)
		scriptContext = jsRunner.global
		JSSerializer.initInstance(jsRunner.global)

		Map<String, Object> jsBinding = [:]
		jsBinding.gravyModule = this
		jsBinding.config = config.toProperties()

		// add module exports to the script scope (app only)
		if ( imports ) prepareImports(imports)

		ScriptableObject exports = (ScriptableObject)jsRunner.run(scriptFile.name, jsBinding)

		prepareExports exports
	}

	@CompileStatic private void prepareImports(Map<String, Handler> imports) {
		Context ctx = Context.enter()
		try {
			NativeFunction prepareImports = (NativeFunction)((ScriptableObject)scriptContext).get('prepareImports', (ScriptableObject)scriptContext)
			prepareImports.call(ctx, (ScriptableObject)scriptContext, (ScriptableObject)scriptContext, [imports, scriptContext] as Object[])
		}
		finally {
			ctx.exit()
		}
	}

	@CompileStatic private Map<String, Handler> prepareExports(ScriptableObject exports) {
		Context ctx = Context.enter()
		try {
			NativeFunction prepareExports = (NativeFunction)((ScriptableObject)scriptContext).get('prepareExports', (ScriptableObject)scriptContext)
			(Map<String, Handler>) ctx.jsToJava(prepareExports.call(ctx, (ScriptableObject)scriptContext, (ScriptableObject)scriptContext, [exports] as Object[]), Map.class)
		}
		finally {
			ctx.exit()
		}
	}

}