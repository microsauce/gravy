package org.microsauce.gravy.context.groovy

import org.microsauce.gravy.json.GravyJsonSlurper;

import groovy.transform.CompileStatic;

class GroovyExportHandler extends GroovyHandler {
	
	boolean serialized // TODO this property will be set by the module loader
					// TODO create a new app config property and a new module property:
							// type: JS, Groovy
	
	@CompileStatic public Object doExecute(Object ... params) {
		if ( serialized ) {
			List<Object> deserializedParms = new ArrayList<Object>()
			GravyJsonSlurper slurper = new GravyJsonSlurper()
			params.each { String param ->
				deserializedParms
			}
			params = deserializedParms
		}
		super.doExecute(params)
	}
}
