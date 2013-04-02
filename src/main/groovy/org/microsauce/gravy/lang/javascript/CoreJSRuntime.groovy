package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic

import org.mozilla.javascript.ScriptableObject

class CoreJSRuntime extends JSRuntime {

    public CoreJSRuntime(List<File> roots) {
        super(roots, null)
    }

    @CompileStatic
    String[] getCoreScripts() {
        ['ringo-extensions.js', 'core.js']
    }

}
