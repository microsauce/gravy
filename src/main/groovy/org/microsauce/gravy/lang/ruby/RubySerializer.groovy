package org.microsauce.gravy.lang.ruby

import groovy.transform.CompileStatic

import org.jruby.RubyObject
import org.jruby.embed.ScriptingContainer
import org.microsauce.gravy.lang.object.Serializer
import org.microsauce.gravy.runtime.GravyThreadLocal

class RubySerializer implements Serializer {

    static RubySerializer instance

    static RubySerializer getInstance() {
        instance
    }

    static void initInstance(ScriptingContainer container) {
        instance = new RubySerializer()
        instance.serializerContainer = container
        instance.serializer = container.get 'serializer'
    }

    private ScriptingContainer serializerContainer;
    private RubyObject serializer;

    @Override
    @CompileStatic
    public Object parse(String string) {
        ScriptingContainer parseContainer = parserContainer()
        RubyObject parser = (RubyObject) parseContainer.get('serializer')
        parserContainer().callMethod(parser, 'parse', [string] as Object[])
    }

    @Override
    @CompileStatic
    public String toString(Object object) {
        (String) serializerContainer.callMethod(serializer, 'to_string', [object] as Object[])
    }

    @CompileStatic
    ScriptingContainer parserContainer() {
        (ScriptingContainer) GravyThreadLocal.SCRIPT_CONTEXT.get() ?: serializerContainer
    }

}
