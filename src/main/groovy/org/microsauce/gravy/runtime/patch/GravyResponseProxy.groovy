package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic

import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.lang.patch.BaseEnterpriseProxy
import org.microsauce.gravy.module.Module
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeJSON
import org.mozilla.javascript.ScriptableObject

abstract class GravyResponseProxy<T extends HttpServletResponse> extends BaseEnterpriseProxy {
	
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
		if ( module.serializeAttributes ) {
			attrModel = stringify(model) 
		}
		request.setAttribute('_model', attrModel) 
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
		((T) target).writer << stringify(model) 
		((T) target).writer.flush()
	}
	
	protected abstract String stringify(Object object) 
	
	protected abstract Object parse(String serializedObject)
	
}

