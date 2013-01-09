package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.tools.shell.Global
import org.ringojs.engine.RhinoEngine
import org.ringojs.engine.RingoConfig
import org.ringojs.repository.FileRepository
import org.ringojs.repository.Repository
import org.ringojs.repository.ZipRepository

@Log4j
abstract class JSRunner { 
	
	RhinoEngine engine 
	Global global
	List<File> roots
	
	JSRunner(List<File> roots) {
		this.roots = roots

		String ringoJarPath = null
		String appRoot = System.getProperty("gravy.appRoot")
		if ( appRoot ) // TODO this is broken (not pointing to WEB-INF)
			ringoJarPath = appRoot+'/lib/ringo-modules.jar'
		else if ( System.getenv()['GRAVY_HOME'] )
			ringoJarPath = System.getenv()['GRAVY_HOME']+'/lib/ringojs/ringo-modules.jar'
		
		Repository ringoRepo = new ZipRepository(ringoJarPath)
		RingoConfig config = new RingoConfig(ringoRepo)
		
		Repository gravyRepo = null
		if (appRoot) { // TODO this is broken gravy js files are not located in the classes folder. how does ringo find them? (the classpath maybe?)
			config.addModuleRepository(new FileRepository(appRoot+'/classes'))
		}
//		String gravyJar = null
//		if ( appRoot )
//			gravyJar = appRoot+'/lib/gravy.jar'
//		else if ( System.getenv()['GRAVY_HOME'] )
//			gravyJar = System.getenv()['GRAVY_HOME']+'/lib/gravy.jar'
//println "gravyRep: $gravyJar"
//		config.addModuleRepository(new ZipRepository(gravyJar))

		if ( roots ) {
			roots.each { File thisRoot ->
				config.addModuleRepository(new FileRepository(thisRoot))
			}
		}

		engine = new RhinoEngine(config, null)
		global = engine.getScope()
		global.put('out', global, System.out)
		global.put('devMode', global, System.getProperty('gravy.devMode')) 
		getCoreScripts().each { String thisScript ->
			engine.runScript(thisScript, [] as String[])
		}
	}
	
	@CompileStatic Object run(String scriptUri, Map<String, Object> binding) {
		Object returnValue = null
println "1"			
		if ( binding ) {
println "2"			
			binding.each { String key, Object value ->
println "3"			
				global.put(key, global, value)
			}
		}
		
		// evaluate application.js
println "4"		
try {	
		engine.runScript(scriptUri, [] as String[])
} catch (Exception e) {
println "4.5"
e.printStackTrace()}
println "5"			
		Scriptable services = (Scriptable) global.get('services', global)
		returnValue = services
		returnValue
	}
	
	abstract String[] getCoreScripts() 
}
