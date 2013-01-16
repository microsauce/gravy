package org.microsauce.gravy.module.ruby;

import groovy.transform.CompileStatic

import org.jruby.RubyObject
import org.jruby.embed.LocalVariableBehavior
import org.jruby.embed.ScriptingContainer
import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.lang.ruby.RubySerializer
import org.microsauce.gravy.module.Module

public class RubyModule extends Module {

	private ScriptingContainer container;
	
	RubyModule() {
		container = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);
		List paths = new ArrayList();
		paths.add("gems/json-1.7.6-java/lib/"); // TODO experiment with this
		paths.add("gems/optruct-0.0.1/lib/");
		container.setLoadPaths(paths)
	}
	
	@Override
	@CompileStatic protected Object doLoad(Map imports) {

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
		
		RubyObject exports = (RubyObject)container.get("services");
		return prepareExports(exports);
	}

	@CompileStatic private void prepareImports(Map<String, Handler> imports) {
		RubyObject importExport = (RubyObject)container.get("import_export");
		RubyObject scope = (RubyObject)container.get("scope");
		container.callMethod(importExport, "prepare_imports", [imports, scope] as Object[]);
	}

	private Map<String, Handler> prepareExports(RubyObject exports) {
		RubyObject importExport = (RubyObject)container.get("import_export");
		return (Map<String, Handler>)container.callMethod(importExport, "prepare_exports", [exports] as Object[]);
	}

}
