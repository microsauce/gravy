package org.microsauce.gravy.lang.javascript

import org.microsauce.gravy.context.ServletFacade
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.lang.object.SerializableObject

import javax.servlet.http.HttpSession

/**
 * TODO rethink this
 *
 * User: microsauce
 * Date: 4/7/13
 * Time: 3:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class JSSessObject extends HashMap {

    def static SESS_PROPERTIES

    HttpSession sess;
    ServletFacade facade

    public JSSessObject(ServletFacade facade) {
        this.sess = facade.nativeReq.session;
        this.facade = facade
    }

    Object get(Object key) {
        if (getProperties().contains(key)) return sess."$key"

		facade.getSessionAttr(key)
//        SerializableObject value = (SerializableObject)sess.getAttribute((String)key);
//        return value == null ? null : value.value(GravyType.JAVASCRIPT);
    }

    Object put(Object key, Object value) {
        if (getProperties().contains(key)) {
            sess."$key" = value
            return value
        }
        else {
			facade.setSessionAttr(key, value)
//            sess.setAttribute((String) key, new SerializableObject(value, GravyType.JAVASCRIPT));
//            return value;
        }
    }

    private def getProperties() {
        if ( !SESS_PROPERTIES ) {
            SESS_PROPERTIES = sess.properties*.key
        }
        SESS_PROPERTIES
    }

}
