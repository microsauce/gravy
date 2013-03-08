package org.microsauce.gravy.lang.coffeescript

import groovy.transform.CompileStatic

import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths

import org.mozilla.javascript.JavaScriptException
import org.ringojs.engine.RhinoEngine

/**
 *
 * Adaptation of Scott Horn's (vertx.io) CoffeeScriptCompiler.java
 *
 */
class CoffeeC {

    private RhinoEngine engine;

    public CoffeeC(RhinoEngine engine) {
        this.engine = engine
        init()
    }

    @CompileStatic
    private init() {
        engine.setOptimizationLevel(-1)
        engine.runScript('coffee-script.js')
    }

    public URI coffeeScriptToJavaScript(URI coffeeScript) throws JavaScriptException,
            InvalidPathException, IOException, URISyntaxException {
        File out = new File(new URI(coffeeScript.toString() + ".js"))
        Path path = Paths.get(coffeeScript)
        String coffee = new String(Files.readAllBytes(path))
        Files.write(out.toPath(), compile(coffee).getBytes())
        return out.toURI()
    }

    public String compile(String coffeeScriptSource) throws JavaScriptException {
        engine.getScope().put("coffeeScriptSource", engine.getScope(), coffeeScriptSource)
        Object src = engine.evaluateExpression("CoffeeScript.compile(coffeeScriptSource);")
        if (src != null) {
            return src.toString()
        } else {
            return null
        }
    }

}
