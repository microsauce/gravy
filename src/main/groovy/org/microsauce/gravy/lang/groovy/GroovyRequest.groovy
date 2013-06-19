package org.microsauce.gravy.lang.groovy

import groovy.transform.CompileStatic

import javax.servlet.http.HttpServletRequest

import org.microsauce.gravy.context.ServletFacade
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.lang.object.ProxyObject
import org.microsauce.gravy.lang.object.SerializableObject
import org.microsauce.gravy.lang.object.SharedObject

/**
 * Created with IntelliJ IDEA.
 * User: microsauce
 * Date: 4/8/13
 * Time: 1:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class GroovyRequest {

    ServletFacade facade
    HttpServletRequest req
    InputStream input
    GroovySession session
    Map json

    @CompileStatic GroovyRequest(ServletFacade facade) {
        this.facade = facade
        this.req = facade.nativeReq
        this.input = (InputStream)facade.getInput()
        this.session = getSession()
        this.json = (Map)facade.getJson()
    }

    def methodMissing(String name, args) {
        req."$name"(*args)
    }
    @CompileStatic Object propertyMissing(String name) {
		facade.getAttr(name)
    }
    @CompileStatic Object propertyMissing(String name, Object value) {
		facade.setAttr(name, value)
    }

    @CompileStatic void next() {
        facade.next()
    }
    @CompileStatic void forward(String uri) {
        facade.forward(uri)
    }

    @CompileStatic GroovySession getSession() {
        GroovySession groovySession = (GroovySession)req.getAttribute('_groovy_session')
        if ( !groovySession ) {
            groovySession = new GroovySession(facade)
            req.setAttribute '_groovy_session', groovySession
        }
        groovySession
    }

}
