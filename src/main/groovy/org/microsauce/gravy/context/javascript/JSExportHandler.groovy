package org.microsauce.gravy.context.javascript

import groovy.transform.CompileStatic

import org.microsauce.gravy.json.GravyJsonSlurper

class JSExportHandler extends JSHandler {
		
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
