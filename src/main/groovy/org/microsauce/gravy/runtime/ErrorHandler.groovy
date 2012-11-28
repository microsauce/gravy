package org.microsauce.gravy.runtime

import groovy.util.logging.Log4j

import org.microsauce.gravy.runtime.ErrorHandler;

import javax.servlet.*

@Log4j
class ErrorHandler {

	def static instance

	static ErrorHandler getInstance(config) {
		if ( instance == null ) instance = new ErrorHandler(config)
		instance
	}

	static ErrorHandler getInstance() {
		if ( instance == null ) throw new RuntimeException('ErrorHandler not properly initialized.')
		instance
	}

	def config

	private ErrorHandler(config) {
		this.config = config
	}

	def errorCode() {
		def addr = InetAddress.getLocalHost();
	    byte[] ipAddr = addr.getAddress();
    	def hostName = addr.getHostName()
		"${hostName}-${System.currentTimeMillis()}".toString()
	}
	
	void handleError(Integer errorType, String message, ServletRequest req, ServletResponse res, Throwable exception) {
		def errorCode = errorCode()
		log.error("*** ${errorCode} *** ${message}", exception)
		req.setAttribute('_model', [errorType: errorType, message: message, errorCode: errorCode])
		req.setAttribute('_view', config.gravy.errorPage)
		def rd = req.getRequestDispatcher(config.gravy.viewUri)
		rd.forward(req, res)
	}

}