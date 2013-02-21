package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic;

import javax.servlet.http.HttpServletResponse;

interface GravyHttpServletResponse extends HttpServletResponse {
	void render(String viewUri, Object model)
    void renderJson(Object model) // TODO lang specific
    void print(String output)
	void write(byte[] output)
	void redirect(String url)
    Object getOut()
    void setOut(Object out)
}
