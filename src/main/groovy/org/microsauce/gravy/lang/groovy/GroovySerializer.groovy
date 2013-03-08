package org.microsauce.gravy.lang.groovy;

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import java.util.regex.Pattern
import org.microsauce.gravy.json.GravyJsonSlurper
import org.microsauce.gravy.lang.object.Serializer

public class GroovySerializer implements Serializer {

    Pattern datePattern = ~/[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}/
    GravyJsonSlurper jsonSlurper = new GravyJsonSlurper()

    Closure jsonReviver = { k, val ->
        def retValue = val
        if (val instanceof String && val.length() >= 19) {
            def substr = val.substring(0, 19)

            if (substr ==~ datePattern) {
                retValue = Date.parse("yyyy-MM-dd'T'HH:mm:ss", substr)
            }
        }
        retValue
    }

    @CompileStatic
    public Object parse(String string) {
        jsonSlurper.parseText(string, jsonReviver)
    }

    @CompileStatic
    public String toString(Object object) {
        new JsonBuilder(object).toString()
    }

}
