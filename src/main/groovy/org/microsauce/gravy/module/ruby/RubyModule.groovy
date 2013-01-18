package org.microsauce.gravy.module.ruby;

import groovy.transform.CompileStatic

import org.jruby.RubyObject
import org.jruby.embed.LocalContextScope
import org.jruby.embed.LocalVariableBehavior
import org.jruby.embed.ScriptingContainer
import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.lang.ruby.RubySerializer
import org.microsauce.gravy.module.Module

public class RubyModule extends Module {

	private ScriptingContainer container;
	
	RubyModule() {
		// SINGLETHREADED is 'thread indifferent' and not a singleton
		container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.PERSISTENT);
	}
	
	@Override
	@CompileStatic protected Object doLoad(Map imports) {

		List<String> paths = [this.folder.absolutePath+'/lib']
		container.setLoadPaths(paths)
		
		scriptContext = container;
		
		container.put("j_gravy_module", this);
		container.put("j_properties", config.toProperties());
		container.put("j_mod_lib_path", folder.getAbsolutePath()+"/lib");
		container.put("j_gem_home", config.get("gem_home")); 

		try {
			InputStream scriptStream = this.getClass().getResourceAsStream("/gravy.rb");
			container.runScriptlet(scriptStream, "gravy.rb");
			// add module exports to the script scope (app only)
			if ( imports != null && imports.size() > 0 ) prepareImports(imports);
			
			container.runScriptlet(new FileInputStream(new File(folder, "application.rb")), "application.rb");
			RubySerializer.initInstance(container);
		}
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
		
		RubyObject exports = (RubyObject)container.get("exp");
		return prepareExports(exports);
	}

	@CompileStatic private void prepareImports(Map<String, Handler> imports) {
		RubyObject importExport = (RubyObject)container.get("import_export");
		RubyObject scope = (RubyObject)container.get("scope");
		Object importMap = (Map)container.callMethod(importExport, "prepare_imports", [imports, scope] as Object[]);
		importMap.each { String modName, Object modImport -> 
			container.put(modName, modImport)
		}
	}

	private Map<String, Handler> prepareExports(RubyObject exports) {
		RubyObject importExport = (RubyObject)container.get("import_export");
		return (Map<String, Handler>)container.callMethod(importExport, "prepare_exports", [exports] as Object[]);
	}

}
