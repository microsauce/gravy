package org.microsauce.gravy.context.groovy

import groovy.transform.CompileStatic

import java.lang.reflect.Proxy

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.GravyServletWrapper
import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.runtime.patch.GravyHttpServletRequest
import org.microsauce.gravy.runtime.patch.GravyHttpServletResponse
import org.microsauce.gravy.runtime.patch.GravyHttpSession
import org.microsauce.gravy.runtime.patch.GravyRequestProxy
import org.microsauce.gravy.runtime.patch.GravyResponseProxy
import org.microsauce.gravy.runtime.patch.GravySessionProxy

class GroovyHandler extends Handler {

    private Closure closure

    GroovyHandler(Closure closure) {
        this.closure = closure
    }

    @CompileStatic
    Object wrapInputStream(InputStream inputStream) {
        return inputStream
    }

    @Override
    @CompileStatic public Object doExecute(GravyServletWrapper wrapper) {
        wrapper.nativeReq.getAttribute('_')
    // TODO don't forget stage 2 caching
    // TODO cache the binding 'binding' in the req
        HttpServletRequest nativeReq = wrapper.nativeReq
        Map binding = (Map)nativeReq.getAttribute('_groovy_binding')
        if ( !binding ) {
            // add the jee runtime to the closure binding
            binding = [:]
            nativeReq.setAttribute('_groovy_binding', binding)

            GravyHttpServletRequest req = wrapper.getReq(GravyType.GROOVY)
            GravyHttpServletResponse res = wrapper.getRes(GravyType.GROOVY)
            OutputStream out = (OutputStream)res.getOut()
            binding.req = req
            binding.sess = req.getSession()
            binding.res = res
            binding.out = out
            binding.writer = new PrintWriter(new OutputStreamWriter(out, 'utf-8'))
            binding.chain = wrapper.nativeChain
            binding.json = wrapper.json
// TODO review JS and Ruby and GravyServletWrapper parms refers to request parameters (not uri parameters)
            if (nativeReq.method == 'GET' || nativeReq.method == 'DELETE') binding.query = wrapper.params
            else if (nativeReq.method == 'POST' || nativeReq.method == 'PUT') binding.form = wrapper.params // TODO verify: not exactly sure what params refers to (most likely servlet request params)

            // add uri parameters
            wrapper.paramMap.each { String key, String value ->
                binding[key] = value
            }
            String[] splat = wrapper.splat ?: []

            // add the splat
            binding.splat = splat
        }

        Closure closure = (Closure) closure.clone()

        String[] _paramList =
            closure.maximumNumberOfParameters == wrapper.splat.size() ?
                wrapper.paramList as String[] : [] as String[]

        closure.delegate = binding as Binding
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call(_paramList.length == 1 ? _paramList[0] : _paramList)
    }

    @CompileStatic
    public Object doExecute(Object params) {
        Closure closure = (Closure) closure.clone()
        closure.call(params)
    }


}