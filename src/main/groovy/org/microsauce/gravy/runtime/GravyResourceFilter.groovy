package org.microsauce.gravy.runtime

import groovy.transform.CompileStatic

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse
import org.microsauce.gravy.util.MimeTable

import org.microsauce.gravy.runtime.resolver.ResourceResolver


/**
 *
 * This class implements a static file service.
 * 
 * M2:
 * 	- implement caching as SEPARATE service
 *  - caching rules based on callbacks (which may be defined in conf.groovy or the app script via 'cache' method)
 *
 * @author microsauce
 *
 */
class GravyResourceFilter implements Filter {

    ResourceResolver resolver
    MimeTable mimeTable

    GravyResourceFilter(List<String> roots, String resourceRoot) {
        resolver = new ResourceResolver(resourceRoot)
        resolver.roots.addAll(roots)
        mimeTable = new MimeTable()
    }

    @CompileStatic
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletRequest req = (HttpServletRequest) request
        HttpServletResponse res = (HttpServletResponse) response
        String uri = req.requestURI

        byte[] fileBytes = resolver.retrieve(uri)
        res.contentType = mimeTable.mimeType(extension(uri)) ?: 'application/octet-stream' 
        res.outputStream.write fileBytes
        res.outputStream.flush()
    }

    @CompileStatic
    private String extension(String uri) {
        int ndx = uri.lastIndexOf('.')
        if (ndx != -1) return uri.substring(ndx + 1, uri.length()).toLowerCase()
        else null
    }

    void destroy() {}

    void init(javax.servlet.FilterConfig config) {}

}
