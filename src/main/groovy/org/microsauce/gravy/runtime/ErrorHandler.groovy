package org.microsauce.gravy.runtime

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Log4j
class ErrorHandler {

//	static ErrorHandler instance

//	static ErrorHandler initInstance(String errorPage, String viewUri) {
//		if ( instance == null ) instance = new ErrorHandler(errorPage, viewUri)
//		instance
//	}

//	static ErrorHandler getInstance() {
//		if ( instance == null ) throw new RuntimeException('ErrorHandler not properly initialized.')
//		instance
//	}

	String errorPage
	String viewUri

	ErrorHandler(String errorPage, String viewUri) {
		this.errorPage = errorPage
		this.viewUri = viewUri
	}

	@CompileStatic
	String errorCode() {
		def addr = InetAddress.getLocalHost();
	    byte[] ipAddr = addr.getAddress();
    	def hostName = addr.getHostName()
		"${hostName}-${System.currentTimeMillis()}".toString()
	}
	
	@CompileStatic
	void handleError(Integer errorType, String message, ServletRequest req, ServletResponse res, Throwable exception) {
		def errorCode = errorCode()
		log.error("*** ${errorCode} *** ${message}", exception)
		if ( errorPage && viewUri ) {
			req.setAttribute('_model', [errorType: errorType, message: message, errorCode: errorCode])
			req.setAttribute('_view', errorPage)
			def rd = ((HttpServletRequest)req).getRequestDispatcher(viewUri)
			rd.forward(req, res)
		}
		else {
			HttpServletResponse _res = (HttpServletResponse) res
			HttpServletRequest _req = (HttpServletRequest) req
			_res.writer << "Error ${errorType}.  There was an error loading requested resource ${_req.requestURI}: ${message}"
			_res.writer.flush()
		}
	}

}