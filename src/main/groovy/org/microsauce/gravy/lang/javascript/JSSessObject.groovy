package org.microsauce.gravy.lang.javascript

import org.microsauce.gravy.context.ServletFacade
import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

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

        CommonObject value = (CommonObject)sess.getAttribute((String)key);
        return value == null ? null : value.value(GravyType.JAVASCRIPT);
    }

    Object put(Object key, Object value) {
        if (getProperties().contains(key)) {
            sess."$key" = value
            return value
        }
        else {
            sess.setAttribute((String) key, new CommonObject(value, GravyType.JAVASCRIPT));
            return value;
        }
    }

    private def getProperties() {
        if ( !SESS_PROPERTIES ) {
            SESS_PROPERTIES = sess.properties*.key
        }
        SESS_PROPERTIES
    }

}
