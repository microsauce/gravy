package org.microsauce.gravy.runtime.patch

import javax.servlet.http.HttpServletRequest

interface GravyHttpServletRequest extends HttpServletRequest {
	Object get(String key)
    Object getIn()
    void setIn(Object _in)
	void put(String key, Object value)
	void next()
	void forward(String uri)
	GravyHttpSession session() // get the patched session
}

