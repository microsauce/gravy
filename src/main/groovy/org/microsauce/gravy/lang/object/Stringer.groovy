package org.microsauce.gravy.lang.object
// use a threadlocale current script context
import groovy.transform.CompileStatic

import org.microsauce.gravy.lang.groovy.GroovySerializer
import org.microsauce.gravy.lang.javascript.JSSerializer
import org.microsauce.gravy.lang.ruby.RubySerializer

class Stringer {

    final Map<String, Serializer> serializers;

    // TODO for now the implementation will be a singleton
    private static Stringer instance

    public static Stringer getInstance() {
        if (!instance) instance = new Stringer()
        return instance
    }

    private Stringer() {
        serializers = new HashMap<String, Serializer>();
        serializers.put(GravyType.GROOVY.name, new GroovySerializer());
        serializers.put(GravyType.JAVASCRIPT.name, JSSerializer.getInstance());
        serializers.put(GravyType.RUBY.name, RubySerializer.getInstance());
    }

    @CompileStatic
    Object parse(String string, GravyType context) {
        return serializers.get(context.name).parse(string);
    }

    @CompileStatic
    String toString(Object object, GravyType context) {
        return serializers.get(context.name).toString(object);
    }

}
