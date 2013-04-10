package org.microsauce.gravy.lang.javascript

import org.codehaus.groovy.runtime.InvokerHelper;
import org.microsauce.gravy.lang.object.CommonObject;
import org.microsauce.gravy.lang.object.GravyType;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

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
    def static FACADE_PROPERTIES = ['session', 'json', 'input', 'facade', 'forward', 'params', 'form', 'query', 'splat']

    private HttpServletRequest req;

    public JSReqObject(HttpServletRequest req) {
        this.req = req;
    }

    Object get(Object key) {
        if (FACADE_PROPERTIES.contains(key)) return super.get(key)
        else if (getProperties().contains(key)) return req."$key"

        CommonObject value = (CommonObject)req.getAttribute((String)key);
        return value == null ? null : value.value(GravyType.JAVASCRIPT);
    }

    Object put(Object key, Object value) {
println "key: $key - value: $value"
        if (FACADE_PROPERTIES.contains(key)) {
            super.put(key, value)
        }
        else if (getProperties().contains(key)) {
            req."$key" = value
            return value
        }
        else {
            req.setAttribute((String) key, new CommonObject(value, GravyType.JAVASCRIPT));
            return value;
        }
    }

    private def getProperties() {
        if ( !REQ_PROPERTIES ) {
            REQ_PROPERTIES = req.properties*.key
        }
        REQ_PROPERTIES
    }

}
