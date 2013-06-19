package org.microsauce.gravy.lang.groovy

import groovy.transform.CompileStatic

import javax.servlet.http.HttpSession

import org.microsauce.gravy.context.ServletFacade
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.lang.object.SerializableObject
import org.microsauce.gravy.lang.object.SharedObject

/**
 * Created with IntelliJ IDEA.
 * User: jboone
 * Date: 4/8/13
 * Time: 2:47 PM
 * To change this template use File | Settings | File Templates.
 */
class GroovySession {

    HttpSession session
    ServletFacade facade

    @CompileStatic GroovySession(ServletFacade facade) {
        this.facade = facade
        this.session = facade.nativeReq.session
    }

    Object methodMissing(String name, Object ... args) {
        session."$name"(*args)
    }
    @CompileStatic Object propertyMissing(String name) {
		facade.getSessionAttr(name)
    }
    @CompileStatic Object propertyMissing(String name, Object value) {
		facade.setSessionAttr(name, value)
    }


}
