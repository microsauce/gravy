package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic

import org.mozilla.javascript.ScriptableObject

class CoreJSRuntime extends JSRuntime {

    public CoreJSRuntime(List<File> roots, ConfigObject coreConfig) {
        super(roots, null, coreConfig)
    }

    public CoreJSRuntime(List<File> roots) {
        this(roots, null, new ConfigObject())
    }

    @CompileStatic
    String[] getCoreScripts() {
        ['coffee-module-loader.js', 'core.js']
    }

}
