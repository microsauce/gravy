package org.microsauce.gravy.util.json

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;

class GroovySerializer implements Serializer {

	@Override
	public String stringify(Object object) {
		new JsonBuilder(object).toString()
	}

	@Override
	public Object parse(String json) {
		new JsonSlurper().parseText(json)
	}

}
