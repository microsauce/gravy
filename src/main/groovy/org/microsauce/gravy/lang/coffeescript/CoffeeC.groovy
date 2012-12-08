package org.microsauce.gravy.lang.coffeescript

import groovy.transform.CompileStatic

import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths

import org.mozilla.javascript.Context
import org.mozilla.javascript.JavaScriptException
import org.mozilla.javascript.Scriptable

/**
 * 
 * Adaptation of Scott Horn's (vertx.io) CoffeeScriptCompiler.java
 * 
 */
class CoffeeC {

	private Scriptable globalScope;

	public CoffeeC(ClassLoader classLoader) {
		init(classLoader)
	}
	
	@CompileStatic
	private init(ClassLoader classLoader) {
		InputStream inputStream = classLoader.getResourceAsStream("coffee-script.js")
		try {
			Reader reader = new InputStreamReader(inputStream, "UTF-8")
			try {
				Context context = Context.enter()
				context.setOptimizationLevel(-1) // Without this, Rhino hits a 64K bytecode limit and fails
				try {
					globalScope = context.initStandardObjects()
					context.evaluateReader(globalScope, reader, "coffee-script.js", 0, null)
				} finally {
					Context.exit()
				}
			} finally {
				reader.close()
			}
		} catch (Exception e) {
			throw new RuntimeException(e)
		} finally {
			try {
				inputStream.close()
			} catch (IOException e) {}
		}
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
		Context context = Context.enter()
		try {
			Scriptable compileScope = context.newObject(globalScope)
			compileScope.setParentScope(globalScope)
			compileScope.put("coffeeScriptSource", compileScope, coffeeScriptSource)

			Object src = context.evaluateString(compileScope,
					String.format("CoffeeScript.compile(coffeeScriptSource);"),
					"CoffeeScriptCompiler", 0, null)
			if (src != null) {
				return src.toString()
			} else {
				return null
			}
		} finally {
			Context.exit()
		}
	}

}
