package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import java.util.regex.Pattern

import org.microsauce.gravy.lang.coffeescript.CoffeeC

@Log4j
class JSLoader {

	static Pattern pattern = ~/[\/a-zA-Z0-9_-]+\.(js|coffee)/
	
	List<File> roots
	
	// TODO check the classpath last
	// TODO when file extension not present on uri assume one ('js' first then 'coffee')
	
	JSLoader(List<File> roots) {
println "JSLoader1"		
		this.roots = roots
println "JSLoader2"		
	}
	
	// TODO static compilation breaks line numbering below 
	
	@CompileStatic public String load(String scriptUri) {
//		for ( File thisRoot in roots ) {
		ResolvedResource resource = getResource(scriptUri)
//			log.info "loading script $scriptUri from script root ${thisRoot.name}"
//			File scriptFile = new File(thisRoot, scriptUri)
		if ( resource && resource.isFound() ) {
			if ( resource.isJS() ) { //  scriptUri.endsWith('.js') ) {
				return resource.text
			}
			else if ( !resource.isJS() ) {
				String script = null
				File compiledScriptFile = new File(resource.absolutePath+'.js')
				if (resource.lastModified > compiledScriptFile.lastModified()) {
					log.info "compiling ${resource.absolutePath} to ${resource.absolutePath}.js"
					CoffeeC coffeec = new CoffeeC(this.class.classLoader)
					script = coffeec.compile resource.text

					println ''
					println '========================================================================='
					println " compiled coffee script: (${compiledScriptFile.name})"
					println '========================================================================='
					println ''
					int lnNbr = 1
					script.eachLine { String line ->
						println "${lnNbr++}: $line"
					}
					println ''
		
					compiledScriptFile.write script
				} else {
					script = compiledScriptFile.text
				}
				
				return script
			}
			

		} else {
			log.warn "script uri $scriptUri not found"
		}
//		}
		
		return null
	}

	@CompileStatic private ResolvedResource getResource(String scriptUri) {
		ResolvedResource resource = null
		List uris = []
println "matches pattern: ${scriptUri ==~ pattern}"
		if ( scriptUri ==~ pattern ) uris << scriptUri
		else uris.addAll([scriptUri+'.js', scriptUri+'.coffee'] as List)
println "uris $uris - scriptUri: $scriptUri"		
		outer: for ( String thisUri in uris ) {
			inner: for ( File thisRoot in roots ) {
				File scriptFile = new File(thisRoot, thisUri)
println "1: $scriptFile"
				if ( scriptFile.exists() ) {
println '2'
					File thisScriptFile = new File(thisRoot, thisUri)
					if ( thisScriptFile.exists() ) {
println '3'
						resource = new ResolvedResource(thisUri, thisScriptFile.absolutePath, 
							thisScriptFile.text, thisScriptFile.lastModified())
						break outer
					}
				}
			}
			if ( !resource ) {
				List cpUris = [thisUri,'modules/'+thisUri]
				for ( String thisCpUri in cpUris ) {
println "4: $thisUri"
					InputStream stream = this.class.classLoader.getResourceAsStream(thisCpUri)
					if ( stream ) {
println '5'
						InputStreamReader reader = new InputStreamReader(stream)
						resource = new ResolvedResource(
							thisUri, null, reader.text, null)
						break
					}
				}
			}
println '6'
			
			if ( resource ) break
println '7'
		}

		resource
	}
		
}