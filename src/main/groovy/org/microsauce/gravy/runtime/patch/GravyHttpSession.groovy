package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic;

import javax.servlet.http.HttpSession;

interface GravyHttpSession extends HttpSession {
	Object get(String key)
	void put(String key, Object value)
	void redirect(String url)
}

