package org.microsauce.gravy.server.runtime

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.ServletResponse
import javax.servlet.ServletRequest
import javax.servlet.Filter
import javax.servlet.FilterChain

import org.microsauce.gravy.app.ApplicationContext;

import groovy.util.logging.Log4j

@Log4j
class GravyFilter implements Filter {
	def filterId
	def actionManager

	GravyFilter(filterId) {
		this.filterId = filterId
		actionManager = ApplicationContext.getInstance()
	}

	void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)  {
		def filter = actionManager.getFilter(filterId)
		filter.delegate = [req : request, res : response, chain : chain] as Binding
		filter.call()
	}
	void destroy(){}
	void init(javax.servlet.FilterConfig config){}
}
