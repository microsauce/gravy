package org.microsauce.gravy.context.groovy

import groovy.transform.CompileStatic
import org.microsauce.gravy.lang.groovy.GroovyRequest
import org.microsauce.gravy.lang.groovy.GroovyResponse

import javax.servlet.http.HttpServletRequest

import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.ServletFacade
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.runtime.patch.GravyHttpServletRequest
import org.microsauce.gravy.runtime.patch.GravyHttpServletResponse

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
    @CompileStatic public Object doExecute(ServletFacade facade) {
        Map groovyFacade = (Map)facade.nativeReq.getAttribute('_groovy_facade')
        if (!groovyFacade) {
            HttpServletRequest nativeReq = facade.nativeReq

            GroovyRequest gReq = new GroovyRequest(facade)
            GroovyResponse gRes = new GroovyResponse(facade)
            Map binding = new HashMap()
            binding.req = gReq
            binding.sess = gReq.session
            binding.res = gRes
            binding.out = gRes.out
            binding.writer = new PrintWriter(new OutputStreamWriter(gRes.out, 'utf-8'))
            binding.chain = facade.nativeChain
            binding.json = facade.getJson()

            if (nativeReq.method == 'GET' || nativeReq.method == 'DELETE') binding.query = facade.requestParams
            else if (nativeReq.method == 'POST' || nativeReq.method == 'PUT') binding.form = facade.requestParams // TODO verify: not exactly sure what uriParamNames refers to (most likely servlet request params)

            // add uri parameters
            facade.uriParamMap.each { String key, String value ->
                binding[key] = value
            }
            String[] splat = facade.splat ?: []

            // add the splat
            binding.splat = splat

            facade.nativeReq.setAttribute('_groovy_facade', binding)
            groovyFacade = binding
        }

        Closure closure = (Closure) closure.clone()   // TODO verify the clone
        List<String> _paramList =
            closure.maximumNumberOfParameters == facade.splat.size() ?
                facade.splat : [] as List<String>

        closure.delegate = groovyFacade as Binding
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call(_paramList.size() == 1 ? _paramList[0] : _paramList)
    }

    @CompileStatic
    public Object doExecute(Object params) {
        Closure closureClone = (Closure) closure.clone()
        closureClone.call(params)
    }


}