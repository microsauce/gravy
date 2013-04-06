package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic

import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.patch.BaseEnterpriseProxy
import org.microsauce.gravy.module.Module

class GravyResponseProxy<T extends HttpServletResponse> extends BaseEnterpriseProxy {

    HttpServletRequest request
    public PrintWriter printer
    public Object out

    GravyResponseProxy(HttpServletResponse res, HttpServletRequest request) {
        super(res)
        res.setContentType('text/html') // set default // TODO make this configurable ???
        this.request = request
        this.out = ((T) target).getOutputStream()
        this.printer = new PrintWriter(new OutputStreamWriter(this.out, 'UTF-8'))   // TODO make character encoding configurable
    }

    @CompileStatic void render(String _viewUri, Object model) {
        Module module = request.getAttribute('_module') as Module
        request.setAttribute('_view', _viewUri)
        request.setAttribute('_model', new CommonObject(model, module))
        request.setAttribute('_render_for_module', module)
        RequestDispatcher dispatcher = request.getRequestDispatcher(module.renderUri)
        ((T) target).contentType = 'text/html'
        dispatcher.forward(request, (T) target)
    }
    /**
     * convenience for js / ruby
     * @param bytes
     */
    @CompileStatic
    void print(String outputStr) {
        this.printer.write(outputStr)
        this.printer.flush() // TODO
    }

    @CompileStatic
    void println(String outputStr) {
        print(outputStr + '\n')
    }
    /**
     * convenience for js / ruby - TODO provide wrapper in sub-class to extract binary for JS / RB
     * @param bytes
     */
    @CompileStatic
    void write(byte[] bytes) {
        ((OutputStream) out).write(bytes)
    }

    @CompileStatic
    void redirect(String url) {
        ((T) target).sendRedirect(url)
    }

    @CompileStatic
    void renderJson(Object model) {
        ((T) target).contentType = 'application/json'
        Module module = ((HttpServletRequest)target).getAttribute('_module') as Module
        printer << new CommonObject(model, module).toString()
        printer.flush()
    }

    Object getOut() { out }

    void setOut(Object out) {this.out = out}     // TODO this is the problem

    PrintWriter getPrinter() { printer }

    void setPrinter(PrintWriter printer) {/*do nothing*/ }


}

