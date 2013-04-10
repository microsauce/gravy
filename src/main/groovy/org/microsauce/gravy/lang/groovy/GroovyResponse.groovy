package org.microsauce.gravy.lang.groovy

import groovy.transform.CompileStatic
import org.microsauce.gravy.context.ServletFacade
import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType

import javax.servlet.http.HttpServletResponse

/**
 * Created with IntelliJ IDEA.
 * User: jboone
 * Date: 4/8/13
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
class GroovyResponse {

    ServletFacade facade
    HttpServletResponse res
    OutputStream out
    PrintWriter printer

    GroovyResponse(ServletFacade facade) {
        this.facade = facade
        this.res = facade.nativeRes
        this.out = facade.getOut()
        this.printer = new PrintWriter(new OutputStreamWriter(this.out, 'UTF-8'))   // TODO make configurable  ???
    }

    def methodMissing(String name, args) {
println "GroovyResponse.methodMissing: $name"
        res."$name"(*args)
    }
    def propertyMissing(String name) {
println "GroovyResponse.methodProperties 1 arg: $name"
        res."$name"
    }
    def propertyMissing(String name, value) {
println "GroovyResponse.methodProperties 2 arg: $name"
        res."$name" = value
    }

    @CompileStatic void print(String str) {
        facade.print(str);
    }
    @CompileStatic void write(byte[] bytes) {
        this.facade.write(bytes)
    }
    @CompileStatic void redirect(String url) {
        facade.redirect(url)
    }
    @CompileStatic renderJson(Object model) {
        facade.renderJson(model)
    }
    @CompileStatic void render(String viewUri, Object model) {
        facade.render(viewUri,model)
    }
}
