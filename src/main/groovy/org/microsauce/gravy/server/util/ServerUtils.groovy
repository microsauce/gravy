package org.microsauce.gravy.server.util

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletResponse
import javax.servlet.ServletRequest
import javax.servlet.Filter
import javax.servlet.FilterChain
import groovy.util.logging.Log4j

@Log4j
class ServerUtils {

	static Map buildContext(HttpServletRequest req, HttpServletResponse res, Class classBinding) {
		def binding = [:]
		res.contentType = 'text/html'
		if ( classBinding ) {
			def className = classBinding.name
			def contextName = className.substring(0,1).toLowerCase() + className.substring(1)
			def classBindingInstance = Mapper.getInstance().bindRequest( classBinding, req )

//			classBindingInstance.metaClass.errors = errors
			binding[contextName] = classBindingInstance
//			binding['errors'] = errors
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

	static String getUri(HttpServletRequest req) {
		String uri
		if (req.getContextPath() != '/')
			uri = req.requestURI.substring(req.contextPath.length())
		else uri = req.requestURI

		uri
	}

}