package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic;

import javax.servlet.http.HttpServletResponse;

interface GravyHttpServletResponse extends HttpServletResponse {
	void render(String viewUri, Object model)
    void renderJson(Object model)
    void print(String output)
    void println(String outputStr)
	void write(byte[] output)
	void redirect(String url)
    Object getOut()
    void setOut(Object out)
}
