package org.microsauce.gravy.lang.ruby

import groovy.transform.CompileStatic

import org.jruby.RubyObject
import org.jruby.embed.ScriptingContainer
import org.microsauce.gravy.lang.object.Serializer

class RubySerializer implements Serializer {

	static RubySerializer instance
	
	static RubySerializer getInstance() {
		instance
	}
	
	static void initInstance(ScriptingContainer container) {
		instance = new RubySerializer()
		instance.container = container
		instance.nativeSerializer = container.get 'serializer'
	}

	private ScriptingContainer container;
	private RubyObject nativeSerializer;
	
	@Override
	@CompileStatic public Object parse(String string) {
		container.callMethod(nativeSerializer, 'parse', [string] as Object[])
	}

	@Override
	@CompileStatic public String toString(Object object) {
		(String)container.callMethod(nativeSerializer, 'to_string', [object] as Object[])
	}

}
