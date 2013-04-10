package org.microsauce.gravy.lang.javascript

import javax.servlet.http.HttpServletResponse

/**
 * Use this in conjunction with ScriptableMap
 * User: microsauce
 * Date: 4/9/13
 * Time: 2:58 PM
 * To change this template use File | Settings | File Templates.
 */
class JSResObject extends HashMap {

    def static RES_PROPERTIES

    HttpServletResponse res

    JSResObject(HttpServletResponse res) {
        this.res = res
    }

    Object get(Object key) {
        if (getProperties().contains(key)) return res."$key"

        super.get(key)
    }

    Object put(Object key, Object value) {
        if (getProperties().contains(key)) {
            res."$key" = value
            return value
        }
        else {
            return super.put(key, value)
        }
    }

    private def getProperties() {
        if ( !RES_PROPERTIES ) {
            RES_PROPERTIES = res.properties*.key
        }
        RES_PROPERTIES
    }

}
