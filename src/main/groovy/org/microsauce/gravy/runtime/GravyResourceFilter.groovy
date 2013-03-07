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
import org.microsauce.gravy.util.MimeTable

import org.microsauce.gravy.runtime.resolver.ResourceResolver

class GravyResourceFilter implements Filter { 

//	static final Pattern jspPattern = ~/.*\.jsp/
//	static final Pattern resourcePattern = ~/.*\.[a-zA-Z0-9]+/

	ResourceResolver resolver
    MimeTable mimeTable

	GravyResourceFilter(List<String> roots, String resourceRoot) {
		resolver = new ResourceResolver(resourceRoot)
		resolver.roots.addAll(roots)
        mimeTable = new MimeTable()
	}

	@CompileStatic
	void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)  {
		HttpServletRequest req = (HttpServletRequest) request
		HttpServletResponse res = (HttpServletResponse) response
		String uri = req.requestURI

        res.contentType = mimeTable.mimeType(extension(uri)) ?: 'application/octet-stream'  // TODO - what should the default be ???
        res.outputStream.write resolver.retrieve(uri)
        res.outputStream.flush()
	}

    @CompileStatic private String extension(String uri) {
        int ndx = uri.lastIndexOf('.')
        if ( ndx != -1 ) return uri.substring(ndx+1, uri.length())
        else null
    }

	void destroy() {}

	void init(javax.servlet.FilterConfig config) {}

}
