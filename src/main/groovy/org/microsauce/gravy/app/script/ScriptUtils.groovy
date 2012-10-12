package org.microsauce.gravy.app.script

import org.microsauce.gravy.app.ApplicationContext
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import groovy.util.logging.Log4j

@Log4j
class ScriptUtils {

	private static CompilerConfiguration compilerConfiguration

	static Object run(Script script) {
		try {
			log.info "executing script ${script.sourceUri}"
			def compConf = compilerConfiguration()
			GroovyScriptEngine gse

			if (script.classLoader)
				gse = new GroovyScriptEngine(
					standardizeRoots(script.roots) as URL[], script.classLoader)
			else				
				gse = new GroovyScriptEngine(
					standardizeRoots(script.roots) as URL[])
			gse.config = compConf

			if (script.classPathUris)
				addCpResources(gse.groovyClassLoader, script.classPathUris)

			Object result = gse.run(script.source, script.binding as Binding)

			return result
		}
		catch (all) {
			log.error "failed to execute script ${script.sourceUri}: ${all.message}", all
			all.printStackTrace()
			return 'error'
		}
	}

	private static standardizeRoots(roots) {

		def standardizeRoots = [] as ArrayList<URL>
		roots.each { thisFolder ->
			standardizeRoots << new File(thisFolder).toURI().toURL()

		}
		standardizeRoots 
	}

	def static private addCpResources(classLoader, urls) {
		urls.each { thisUrl ->
			classLoader.addURL(new File(thisUrl).toURL())
		}
	}

	private static CompilerConfiguration compilerConfiguration() {
		if (!compilerConfiguration) {

			def importCustomizer = new ImportCustomizer()

			importCustomizer.addStarImports('org.microsauce.gravy')
			importCustomizer.addStarImports('org.microsauce.gravy.app')
			importCustomizer.addStarImports('org.microsauce.gravy.server')
			importCustomizer.addStarImports('org.microsauce.gravy.server.runtime')
			importCustomizer.addStarImports('javax.servlet.http')
			importCustomizer.addStarImports('javax.servlet')
			importCustomizer.addStaticStar('groovy.util.logging.Log4j')
			importCustomizer.addStaticStar('javax.servlet.DispatcherType')
			importCustomizer.addStaticStar('org.microsauce.gravy.app.ApplicationContext')

			compilerConfiguration = new CompilerConfiguration(CompilerConfiguration.DEFAULT)
			compilerConfiguration.addCompilationCustomizers(importCustomizer)
			compilerConfiguration.addCompilationCustomizers(new ASTTransformationCustomizer(Log4j))
			compilerConfiguration.minimumRecompilationInterval = 0
		}

		compilerConfiguration
	}

}
