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
	
	GravyResponseProxy(HttpServletResponse res, HttpServletRequest request, String renderUri, Module module) {
		super(res)
		this.request = request
		this.renderUri = renderUri
		this.module = module
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
	@CompileStatic void write(String output) {
		((T) target).writer.write(output)
	}
	@CompileStatic void redirect(String url) {
		((T) target).sendRedirect(url)
	}
	@CompileStatic void renderJson(Object model) {
		((T) target).contentType = 'application/json'
		((T) target).writer << new CommonObject(model, context()).toString() 
		((T) target).writer.flush()
	}
	
	protected Module context() {
        module
    }
	
}

