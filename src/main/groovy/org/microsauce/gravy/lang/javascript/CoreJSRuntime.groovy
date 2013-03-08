package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic

import org.mozilla.javascript.ScriptableObject

class CoreJSRuntime extends JSRuntime {

    public CoreJSRuntime(List<File> roots) {
        super(roots, null)
    }

    @CompileStatic
    String[] getCoreScripts() {
        ['coffee-module-loader.js', 'core.js']
    }

}
