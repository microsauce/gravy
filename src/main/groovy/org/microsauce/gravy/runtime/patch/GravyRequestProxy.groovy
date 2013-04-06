package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic

import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.lang.patch.BaseEnterpriseProxy
import org.microsauce.gravy.module.Module

class GravyRequestProxy<T extends HttpServletRequest> extends BaseEnterpriseProxy {

    FilterChain chain
    HttpServletResponse response
    HttpSession session
    BufferedReader reader
    Object input

    GravyRequestProxy(Object target, HttpServletResponse res, HttpSession session, FilterChain chain) {
        super(target)
        this.response = res
        this.session = session
        this.chain = chain
        this.input = ((T) target).getInputStream()
        this.reader = new BufferedReader(new InputStreamReader(this.input, 'UTF-8')) // TODO make configurable ???
    }

    @CompileStatic
    Object get(String key) {
        Object obj = ((T) target).getAttribute(key)
        Module module = ((HttpServletRequest)target).getAttribute('_module') as Module
        if ( obj ) obj = obj instanceof CommonObject ? ((CommonObject)obj).value(module) : obj
        obj
    }

    @CompileStatic
    void put(String key, Object value) {
        Module module = ((HttpServletRequest)target).getAttribute('_module') as Module
        CommonObject obj = new CommonObject(value, module)
        ((T) target).setAttribute key, obj
    }

    @CompileStatic
    void next() {
        chain.doFilter((T) target, response)
    }

    @CompileStatic
    void forward(String uri) {
        RequestDispatcher dispatcher = ((T) target).getRequestDispatcher(uri)
        dispatcher.forward((T) target, response)
    }

    @CompileStatic
    HttpSession session() {
        session
    }

    Object getIn() {
        input
    }

    Object setIn(Object _in) {}
}

