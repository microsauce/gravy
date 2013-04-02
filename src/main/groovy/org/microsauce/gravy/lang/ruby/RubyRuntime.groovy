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
//class RubyRuntime extends Runtime {
//
//    private ScriptingContainer container;
//    private org.jruby.RubyModule rubyModule;
//
//    @CompileStatic RubyRuntime() {
//        // SINGLETHREADED - supports multiple ruby instances
//        container = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.PERSISTENT);
//    }
//
//    @Override
//    @CompileStatic
//    protected Object doLoad(Map imports) {
//
//        List<String> paths = [this.folder.absolutePath + '/lib']
//        container.loadPaths.addAll(paths)
//
//    }
//}