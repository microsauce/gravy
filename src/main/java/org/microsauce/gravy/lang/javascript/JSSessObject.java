package org.microsauce.gravy.lang.javascript;

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

    private HttpSession sess;

    public JSSessObject(HttpSession sess) {
        this.sess = sess;
    }

    public Object get(Object key) {
        Object value = super.get(key);
        if ( value == null ) {
            String strKey = (String) key;
            value = sess.getAttribute(strKey);
            super.put(strKey, value);
        }
        return value;
    }

    public Object put(Object key, Object value) {
        super.put(key, value);
        sess.setAttribute((String) key, value);
        return value;
    }

}
