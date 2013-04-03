package org.microsauce.gravy.lang.ruby

import groovy.transform.CompileStatic
import org.jruby.embed.LocalContextScope
import org.jruby.embed.LocalVariableBehavior
import org.jruby.embed.ScriptingContainer

/**
 * Created with IntelliJ IDEA.
 * User: microsuace
 * Date: 3/31/13
 * Time: 10:54 PM
 * To change this template use File | Settings | File Templates.
 */
class RubyRuntime {

    private ScriptingContainer container;
    private org.jruby.RubyModule rubyModule;

    @CompileStatic RubyRuntime(List<String> roots) {
        // SINGLETHREADED - supports multiple ruby instances
        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.PERSISTENT);
        if ( roots ) container.loadPaths.addAll(roots)

        InputStream scriptStream = this.getClass().getResourceAsStream("/gravy.rb");
        container.runScriptlet(scriptStream, "gravy.rb");
    }

    @CompileStatic Object run(String scriptlet, String path) {
        run scriptlet, path, [:]
    }

    @CompileStatic Object run(String scriptlet, String path, Map<String, Object> binding) {
        binding.each { String key, Object value ->
            container.put(key, value)
        }
        container.runScriptlet(new StringReader(scriptlet), path);
    }

    @CompileStatic void appendRoots(List<String> roots) {

        roots.each { String thisRoot ->
            container.loadPaths.addAll(roots)
        }
    }

}