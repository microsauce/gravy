package org.microsauce.gravy.lang.groovy

import groovy.transform.CompileStatic
import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType

import javax.servlet.http.HttpSession

/**
 * Created with IntelliJ IDEA.
 * User: jboone
 * Date: 4/8/13
 * Time: 2:47 PM
 * To change this template use File | Settings | File Templates.
 */
class GroovySession {

    HttpSession session

    GroovySession(HttpSession session) {
        this.session = session
    }

    Object methodMissing(String name, Object ... args) {
        session."$name"(*args)
    }
    @CompileStatic Object propertyMissing(String name) {
        Object attr = session.getAttribute(name);
        if ( attr != null && attr instanceof CommonObject ) return ((CommonObject)attr).value(GravyType.GROOVY)
        else return attr
    }
    @CompileStatic Object propertyMissing(String name, Object value) {
        session.setAttribute(name, new CommonObject(value, GravyType.GROOVY));
    }


}
