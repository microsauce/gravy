package org.microsauce.gravy.lang.javascript

import groovy.transform.CompileStatic
import org.microsauce.gravy.context.ServletFacade
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.lang.object.SerializableObject;

import javax.servlet.http.HttpServletRequest

/**
 * TODO rethink this
 *
 * User: microsauce
 * Date: 4/7/13
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class JSReqObject extends HashMap {

    def static REQ_PROPERTIES
	def static REQ_METHODS
    def static FACADE_PROPERTIES = [
		'session', 'json', 'input', 'facade', 
		'forward', 'params', 'form', 'query', 
		'splat', 'next', 'initialize', '__noSuchMethod__'
	] 
	
    private HttpServletRequest req;
    private ServletFacade facade;

    @CompileStatic public JSReqObject(ServletFacade facade) {
        this.req = facade.nativeReq;
        this.facade = facade;
    }

    Object get(Object key) {
        if (FACADE_PROPERTIES.contains(key)) return super.get(key)
        else if (getProperties().contains(key)) return req."$key"
        else if (getMethods().contains(key)) return null // force __noSuchMethod__
		return facade.getAttr(key)
    }

    Object put(Object key, Object value) {
        if (FACADE_PROPERTIES.contains(key)) {
            super.put(key, value)
        } else if (getProperties().contains(key)) {
            req."$key" = value
            return value
        } else {
			facade.setAttr(key, value)
			return value
        }
    }

    private def getProperties() {
        if ( !REQ_PROPERTIES ) {
            REQ_PROPERTIES = req.properties*.key
        }
        REQ_PROPERTIES
    }
	
	private def getMethods() {
		if ( !REQ_METHODS ) {
			REQ_METHODS = req.metaClass.methods*.name
		}
		REQ_METHODS
	}

}
