package org.microsauce.gravy.module.ruby;

import groovy.transform.CompileStatic

import org.jruby.embed.ScriptingContainer
import org.microsauce.gravy.lang.ruby.RubyRuntime
import org.microsauce.gravy.lang.ruby.RubySerializer
import org.microsauce.gravy.module.Module
import org.jruby.RubyIO

public class RubyModule extends Module {

    private ScriptingContainer container;
    private org.jruby.RubyModule rubyModule;
    private RubyRuntime runtime

    RubyModule() {}

    @Override
    @CompileStatic
    protected Object doLoad() {

        if ( name != 'app' )
            runtime.appendRoots([this.folder.absolutePath, new File(folder, '/lib').absolutePath] as List<String>)

        scriptContext = runtime.container
        container = runtime.container

        try {
            rubyModule = (org.jruby.RubyModule) runtime.run(
                assembleModuleScript(name, new File(folder, "application.rb")),
                "${folder.absolutePath}/application.rb", [
                    ("j_${name}_module".toString()) : this,
                    ("j_${name}_conf".toString()) : config,
                    ("j_${name}_log".toString()) : moduleLogger
            ]);
            RubySerializer.initInstance(container);
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @CompileStatic
    private String assembleModuleScript(String moduleName, File scriptFile) {
        String rawScriptText = scriptFile.text
        // to preserve line numbering in user script define all of this on the first line
        """\$j_${moduleName}_log = j_${moduleName}_log; \$j_${moduleName}_conf = j_${moduleName}_conf; \$j_${moduleName}_module = j_${moduleName}_module; module Gravy_Module_$moduleName; include GravyModule; extend self; @j_module = \$j_${moduleName}_module; log = \$j_${moduleName}_log; conf = config_to_ostruct_recursive(\$j_${moduleName}_conf); $rawScriptText
          end
		  Gravy_Module_$moduleName
		""".toString()
    }

    @CompileStatic Object wrapInputStream(InputStream inputStream) {
        new RubyIO(container.getProvider().getRuntime(), inputStream);
    }

}
