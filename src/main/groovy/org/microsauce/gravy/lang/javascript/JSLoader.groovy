package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import org.microsauce.gravy.lang.coffeescript.CoffeeC

@Log4j
class JSLoader {

	List<File> roots
	
	JSLoader(List<File> roots) {
		this.roots = roots
	}
	
	// TODO static compilation breaks line numbering below 
	public String load(String scriptUri) {
		for ( File thisRoot in roots ) {
			log.info "loading script $scriptUri from script root ${thisRoot.name}"
			File scriptFile = new File(thisRoot, scriptUri)
			if ( scriptFile.exists() ) {
				if ( scriptUri.endsWith('.js') ) {
					return scriptFile.text
				}
				else if ( scriptUri.endsWith('.coffee') ) {
					String script = null
					File compiledScriptFile = new File(scriptFile.absolutePath+'.js')
					if (scriptFile.lastModified() > compiledScriptFile.lastModified()) {
						log.info "compiling ${scriptFile.absolutePath} to ${scriptFile.absolutePath}.js"
						CoffeeC coffeec = new CoffeeC(this.class.classLoader)
						script = coffeec.compile scriptFile.text
	
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
				
				break
			} else {
				log.warn "script uri $scriptUri not found in script root ${thisRoot.absolutePath}/lib"
			}
		}
		
		return null
	}

	
}
