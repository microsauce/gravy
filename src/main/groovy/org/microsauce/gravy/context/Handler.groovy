package org.microsauce.gravy.context

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.module.Module

import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.regex.Pattern

/**
 * A request handler defined in an application script.
 * 
 * @author jboone
 */
@Log4j
abstract class Handler {
	
	protected Module module

	abstract Object doExecute(HttpServletRequest req, HttpServletResponse res, FilterChain chain, HandlerBinding handlerBinding, Map parms)
	abstract Object doExecute(Object params)

	public Module getModule() {
		return module;
	}
	
	public void setModule(Module module) {
		this.module = module;
	}
	
	@CompileStatic public Object call( 
		CommonObject parm1,
		CommonObject parm2,
		CommonObject parm3,
		CommonObject parm4,
		CommonObject parm5,
		CommonObject parm6,
		CommonObject parm7) {

		List parms = new ArrayList()
		Object np7 = nativeObj(parm7)
		if (np7) parms.add(0, np7)
		Object np6 = nativeObj(parm6)
		if (np6) parms.add(0, np6)
		Object np5 = nativeObj(parm5)
		if (np5) parms.add(0, np5)
		Object np4 = nativeObj(parm4)
		if (np4) parms.add(0, np4)
		Object np3 = nativeObj(parm3)
		if (np3) parms.add(0, np3)
		Object np2 = nativeObj(parm2)
		if (np2) parms.add(0, np2)
		Object np1 = nativeObj(parm1)
		if (np1) parms.add(0, np1)
		Object result = doExecute(parms)

		new CommonObject(result, module).toNative()
	}
		
	@CompileStatic private Object nativeObj(CommonObject obj) {
		obj ? obj.value(module) : null
	}

	@CompileStatic
	Object execute(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Pattern uriPattern, List<String> params) {
		HandlerBinding handlerBinding = new HandlerBinding(req, res, uriPattern, params)
		try {
			doExecute(req, res, chain, handlerBinding, parseRequest(req))
		}
		catch ( Throwable t ) {
			org.microsauce.gravy.runtime.Error error = new org.microsauce.gravy.runtime.Error(t)
			log.error "${error.errorCode} - ${error.errorMessage}", t
			req.setAttribute("error", error)
			RequestDispatcher dispatcher = req.getRequestDispatcher(module.errorUri)
			dispatcher.forward(req, res)
		}
	}

    protected abstract Object wrapInputStream(InputStream inputStream);

    // TODO examine this - the servlet API adds form data and query string data to the parameter map,
    @CompileStatic private Map parseRequest(HttpServletRequest req) {
        Map map = [:]
        if ( (req.method == 'POST' || req.method == 'PUT') && ServletFileUpload.isMultipartContent(req) ) {
            try {
                List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req); // TODO add config options
                for (item in items) {
                    if (item.isFormField()) map[item.getFieldName()] = item.getString()
                    else map[item.getFieldName()] = wrapInputStream(item.getInputStream())
                }
            } catch (FileUploadException e) {
                throw new ServletException("Failed to parse multi-part request.", e);
            }
        }
        else {
            req.getParameterNames().each { String key ->
                map[key] = req.getParameter(key)
            }
        }
        return map
    }
	
	@CompileStatic Object execute(Object ... parms) {
		doExecute(parms)
	}
	
	@CompileStatic Object execute() {
		doExecute([] as Object[])
	}
	
}