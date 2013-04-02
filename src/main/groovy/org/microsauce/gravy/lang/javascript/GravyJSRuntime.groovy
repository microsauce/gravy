package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.mozilla.javascript.ScriptableObject

class GravyJSRuntime extends JSRuntime {

    public GravyJSRuntime(List<File> roots) {
        super(roots)
    }

    @CompileStatic
    String[] getCoreScripts() {
        ['ringo-extensions.js','core.js', 'gravy.js']
    }

}
