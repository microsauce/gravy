package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic;

import javax.servlet.http.HttpServletResponse;

interface GravyHttpServletResponse extends HttpServletResponse {
	void render(String viewUri, Object model)
	void write(String output)
	void redirect(String url)
}
