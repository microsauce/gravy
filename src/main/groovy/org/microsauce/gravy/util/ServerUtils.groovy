package org.microsauce.gravy.util

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


// TODO delete this
@Log4j
class ServerUtils {

	/**
	 * TODO this need to move to the Groovy Action/Handler implementation
	 */
	@CompileStatic
	static Map buildContext(HttpServletRequest req, HttpServletResponse res, Class classBinding) {
		
		def binding = [:]
		res.contentType = 'text/html'
		if ( classBinding ) {
			def className = classBinding.name
			def contextName = className.substring(0,1).toLowerCase() + className.substring(1)
			def classBindingInstance = Mapper.getInstance().bindRequest( classBinding, req )

			binding[contextName] = classBindingInstance
		}

		binding.req = req
		binding.res = res
		binding.out = res.writer
		binding.sess = req.session

		binding
	}

	def static prependContextPath(str, contextPath) {
		def cp = contextPath ?: '/'
		cp == '/' ? str : cp+str
	}

	@CompileStatic
	static String getUri(HttpServletRequest req) {		
		String uri
		if (req.getContextPath() != '/')
			uri = req.requestURI.substring(req.contextPath.length())
		else uri = req.requestURI

		uri
	}

}