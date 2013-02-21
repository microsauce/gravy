package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic

import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType;
import org.microsauce.gravy.lang.patch.BaseEnterpriseProxy
import org.microsauce.gravy.module.Module

class GravyResponseProxy<T extends HttpServletResponse> extends BaseEnterpriseProxy {
	
	HttpServletRequest request
	String renderUri
	Module module
    PrintWriter writer
    public Object out
	
	GravyResponseProxy(HttpServletResponse res, HttpServletRequest request, String renderUri, Module module) {
		super(res)
		this.request = request
		this.renderUri = renderUri
		this.module = module
        this.out = ((T)target).getOutputStream()
        this.writer = new PrintWriter(new OutputStreamWriter(this.out, 'utf-8'))
	}
	
	@CompileStatic void render(String _viewUri, Object model) {
		request.setAttribute('_view', _viewUri)
		Object attrModel = model
		request.setAttribute('_model', new CommonObject(model, context())) 
		request.setAttribute('_module', module)
		RequestDispatcher dispatcher = request.getRequestDispatcher(renderUri)
		((T) target).contentType = 'text/html'
		dispatcher.forward(request, (T) target)
	}
    /**
     * convenience for js / ruby
     * @param bytes
     */
	@CompileStatic void print(String outputStr) {
		this.writer.write(outputStr)
        this.writer.flush() // TODO
	}
    /**
     * convenience for js / ruby - TODO provide wrapper in sub-class to extract binary for JS / RB
     * @param bytes
     */
    @CompileStatic void write(byte[] bytes) {
        ((OutputStream)out).write(bytes)
    }
	@CompileStatic void redirect(String url) {
		((T) target).sendRedirect(url)
	}
	@CompileStatic void renderJson(Object model) {
		((T) target).contentType = 'application/json'
		writer << new CommonObject(model, context()).toString()
		writer.flush()
	}

    Object getOut() {out}

    void setOut(Object out) {/*do nothing*/}

    protected Module context() {
        module
    }
	
}

