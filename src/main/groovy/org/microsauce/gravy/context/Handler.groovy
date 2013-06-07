package org.microsauce.gravy.context

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.module.Module

import javax.servlet.DispatcherType
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * A request handler as defined in an application script.
 *
 * @author microsauce
 */
@Log4j
abstract class Handler {

    protected Module module
    EnterpriseService service

    abstract Object doExecute(ServletFacade wrapper)

    abstract Object doExecute(Object params)

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    @CompileStatic Object execute(ServletFacade servletFacade) {
        try {
            servletFacade.currentContext = module.type
            doExecute(servletFacade)
        }
        catch (Throwable t) {
            org.microsauce.gravy.runtime.Error error = new org.microsauce.gravy.runtime.Error(t)
            log.error "${error.errorCode} - ${error.message}", t
            HttpServletRequest req = servletFacade.getNativeReq()
            HttpServletResponse res = servletFacade.getNativeRes()
            if ( module.context.findService('/error') && !req.requestURI == '/error') {
                req.setAttribute("error", error)
                RequestDispatcher dispatcher = req.getRequestDispatcher(module.errorUri)
                dispatcher.forward(req, res)
            } else {
                res.contentType = 'text/plain'
                res.status = 500
                res.outputStream.print(
"""
${error.errorCode} - ${error.message } \n
${error.stackTrace}
""")
                res.outputStream.flush()
            }

        }
    }

    @CompileStatic Object execute(Object... parms) {
        doExecute(parms)
    }

    @CompileStatic Object execute() {
        doExecute([] as Object[])
    }

}