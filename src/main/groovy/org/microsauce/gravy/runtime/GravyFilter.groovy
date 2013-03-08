package org.microsauce.gravy.runtime

import groovy.util.logging.Log4j

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

@Log4j
class GravyFilter implements Filter {
    def filterId
    def actionManager

    GravyFilter(filterId) {
        this.filterId = filterId
        actionManager = ApplicationContext.getInstance()
    }

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        def filter = actionManager.getFilter(filterId)
        filter.delegate = [req: request, res: response, chain: chain] as Binding
        filter.call()
    }

    void destroy() {}

    void init(javax.servlet.FilterConfig config) {}
}
