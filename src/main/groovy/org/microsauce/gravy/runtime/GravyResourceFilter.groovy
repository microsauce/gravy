package org.microsauce.gravy.runtime

import groovy.transform.CompileStatic

import java.util.regex.*

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.runtime.resolver.ResourceResolver

class GravyResourceFilter implements Filter { 

	static final Pattern jspPattern = ~/.*\.jsp/
	static final Pattern resourcePattern = ~/.*\.[a-zA-Z0-9]+/

	ResourceResolver resolver

	GravyResourceFilter(List<String> roots, String resourceRoot) {
		resolver = new ResourceResolver(resourceRoot)
		resolver.roots.addAll(roots)
	}

	@CompileStatic
	void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)  {
		HttpServletRequest req = (HttpServletRequest) request
		HttpServletResponse res = (HttpServletResponse) response
		String uri = req.requestURI
		if ( uri ==~ jspPattern ) {
			String realUri = resolver.realUri uri
			RequestDispatcher dispatcher = req.getRequestDispatcher realUri
			dispatcher.forward(request, response)
		} else if ( uri ==~ resourcePattern ) {
			// handle static resources
			res.outputStream.write resolver.retrieve(uri)
			res.outputStream.flush()
		} else chain.doFilter request, response
	}

	void destroy() {}

	void init(javax.servlet.FilterConfig config) {}

}
