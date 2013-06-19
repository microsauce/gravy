package org.microsauce.gravy.context

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.servlet.FilterChain
import javax.servlet.RequestDispatcher
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.jruby.RubyIO
import org.jruby.embed.ScriptingContainer
import org.jruby.runtime.builtin.IRubyObject
import org.microsauce.gravy.lang.common.RequestForm
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.lang.object.ProxyObject
import org.microsauce.gravy.lang.object.SerializableObject
import org.microsauce.gravy.module.Module
import org.microsauce.incognito.Incognito
import org.mozilla.javascript.*
import org.ringojs.wrappers.Stream

@Log4j
class ServletFacade {

    static ScriptingContainer rubyContext;
    static Scriptable jsContext;

    HttpServletRequest nativeReq
    HttpServletResponse nativeRes
    FilterChain nativeChain
    HttpSession session
	
	Incognito incognito

    Pattern uriPattern
    String requestUri

    Map<String, String> uriParamMap = [:] as LinkedHashMap  // uri named parameter map
    List<String> uriParamValues = []             			// complete uri parameter value list
    List<String> splat = []                 				// wild card 'splat' list
    SerializableObject json                       			// a json payload

    Map<String,Object> requestParams

    OutputStream out
    PrintWriter printer

    GravyType currentContext;
    Map<GravyType,ContextAdaptor> adaptors;

    @CompileStatic
    ServletFacade(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Incognito incognito, Pattern uriPattern, List<String> uriParamNames) {
        init(req, res, chain, uriPattern, uriParamNames)
        session = req.session
        out =  res.getOutputStream()
        printer = new PrintWriter(new OutputStreamWriter(this.out, 'UTF-8'))  // TODO make char encoding configurable ???
        adaptors = new HashMap<GravyType,ContextAdaptor>()
		this.incognito = incognito
		
    }

    @CompileStatic void init(HttpServletRequest req, HttpServletResponse res, 
		FilterChain chain, Pattern uriPattern, List<String> uriParamNames) {

        this.nativeReq = req
        this.nativeRes = res
        nativeRes.contentType = 'text/html'   // default to html
        this.nativeChain = chain

        this.requestUri = req.requestURI
        this.uriPattern = uriPattern

        json = jsonReq(req)
        this.requestParams = requestParameters(req)

        if ( uriPattern ) {
            Matcher matches = requestUri =~ uriPattern
            Integer ndx = 1
            splat = []
            if (uriParamNames && uriParamNames.size() > 0) {
                while (matches.find()) {
                    Integer groupCount = matches.groupCount()
                    for (; ndx <= groupCount; ndx++) {
                        String thisParamName = uriParamNames[ndx - 1]
                        String paramValue = matches.group(ndx)
                        uriParamValues << paramValue
                        if (thisParamName == '*')
                            splat << paramValue
                        else {
                            uriParamMap[thisParamName] = paramValue
                        }
                    }
                }
            } else if (matches.groupCount() > 0) {
                Integer groupCount = matches.groupCount()
                while (matches.find()) {
                    for (; ndx <= groupCount; ndx++) {
                        splat << matches.group(ndx)
                    }
                }
            }
        }
    }

    @CompileStatic
    private String readJsonPayload(HttpServletRequest req) {
        String json = null
        if (req.contentType && req.contentType.startsWith('application/json')) {
            json = req.inputStream.getText('UTF-8')
        }
        json
    }

    @CompileStatic private SerializableObject jsonReq(HttpServletRequest req) {
        String payload = readJsonPayload(req)
        SerializableObject json = null
        if ( payload ) {
            json = new SerializableObject(null, currentContext)
            json.serializedRepresentation = payload
        }
        json
    }

    @CompileStatic private Map requestParameters(HttpServletRequest req) {
        // TODO look into httpservletrequest.getPart/Parts
        RequestForm requestParameters = new RequestForm(this)
        if ((req.method == 'POST' || req.method == 'PUT') && ServletFileUpload.isMultipartContent(req)) {
            try {
                List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req);
                for (item in items) {
                    if (item.isFormField()) requestParameters[item.getFieldName()] = item.getString()
                    else {
                        requestParameters[item.getFieldName()] = item.getInputStream()
                    }
                }
            } catch (FileUploadException e) {
                throw new ServletException("Failed to parse multi-part request.", e);
            }
        } else {
            req.getParameterNames().each { String key ->
                requestParameters[key] = req.getParameter(key)
            }
        }
        return requestParameters
    }

    //
    // public facade methods
    //

    @CompileStatic void setAttr(String name, Object value) {
        nativeReq.setAttribute(name, new ProxyObject(currentContext, value, incognito))
    }

    @CompileStatic Object getAttr(String name) {
        ProxyObject value = (ProxyObject)nativeReq.getAttribute(name)
        value ? value.value(currentContext) : null
    }

    @CompileStatic Object setSessionAttr(String name, Object value) {
        session.setAttribute(name, new ProxyObject(currentContext, value, incognito))
    }

    @CompileStatic Object getSessionAttr(String name) {
        ProxyObject value = (ProxyObject)session.getAttribute(name)
        value ? value.value(currentContext) : null
    }

    @CompileStatic void next() {
        nativeChain.doFilter(nativeReq, nativeRes)
    }

    @CompileStatic void forward(String uri) {
        RequestDispatcher dispatcher = nativeReq.getRequestDispatcher(uri)
        dispatcher.forward(nativeReq, nativeRes)
    }

    @CompileStatic void redirect(String url) {
        nativeRes.sendRedirect(url)
    }

    @CompileStatic void renderJson(Object model) {
        nativeRes.contentType = 'application/json'
        printer << new SerializableObject(model, currentContext).toString()
        printer.flush()
    }

    @CompileStatic void print(String str) {
        printer.print(str)
        printer.flush()
    }

    @CompileStatic void write(Object binaryData) {
        getAdaptor().write(binaryData)
    }

    @CompileStatic void render(String viewUri, Object model) {
        Module module = nativeReq.getAttribute('_module') as Module
        nativeReq.setAttribute('_view', viewUri)
        nativeReq.setAttribute('_model', new SerializableObject(model, module.type))
        nativeReq.setAttribute('_document_root', module.name)
        RequestDispatcher dispatcher = nativeReq.getRequestDispatcher(module.renderUri)
        nativeRes.contentType = 'text/html'
        dispatcher.forward(nativeReq, nativeRes)
    }

    @CompileStatic Object getInput() {
        getAdaptor().getInput()
    }
    @CompileStatic Object getContextualizedInput(InputStream is) {
        getAdaptor().getContextualizedInput(is)
    }
    @CompileStatic Object getOut() {
        getAdaptor().getOut()
    }
    @CompileStatic Object getJson() {
        if ( json ) return json.value(currentContext)
        else return null
    }

    @CompileStatic private ContextAdaptor getAdaptor() {
        ContextAdaptor adaptor = adaptors[currentContext]
        if ( !adaptor ) {
            switch (currentContext) {
                case GravyType.GROOVY:
                    adaptor = new GroovyAdaptor()
                    break
                case GravyType.JAVASCRIPT:
                    adaptor = new JSAdaptor()
                    break
                case GravyType.RUBY:
                    adaptor = new RubyAdaptor()
            }
            adaptors[currentContext] = adaptor
        }

        adaptor
    }

    //
    // Context adaptors
    //
    private interface ContextAdaptor {
        Object getInput()
        Object getOut()
        Object getContextualizedInput(InputStream input)
        void write(Object data)
    }

    private class JSAdaptor implements ContextAdaptor {
        private Object input
        private Object out

        @CompileStatic Object getInput() {
            if ( !input )
                input = new Stream(jsContext, nativeReq.getInputStream(), null)
            input
        }
        @CompileStatic Object getOut() {
            if ( !out )
                out = new Stream(jsContext, nativeRes.getOutputStream(), null)
            out
        }
        @CompileStatic void write(Object data) {
            ((Stream)getOut()).write(data, Undefined.instance, Undefined.instance)
        }
        @CompileStatic Object getContextualizedInput(InputStream is) {
            new Stream(jsContext, is, null)
        }
    }
    private class RubyAdaptor implements ContextAdaptor {
        private Object input
        private Object out

        @CompileStatic Object getInput() {
            if ( !input )
                input = new RubyIO(rubyContext.getProvider().getRuntime(), nativeReq.getInputStream())
            input
        }
        @CompileStatic Object getOut() {
            if ( !out )
                out = new RubyIO(rubyContext.getProvider().getRuntime(), nativeRes.getOutputStream())
            out
        }
        @CompileStatic void write(Object data) {
            ((RubyIO)getOut()).write(rubyContext.getProvider().getRuntime().getCurrentContext(), (IRubyObject)data)
        }
        @CompileStatic Object getContextualizedInput(InputStream is) {
            new RubyIO(rubyContext.getProvider().getRuntime(), is)
        }
    }
    private class GroovyAdaptor implements ContextAdaptor {
        @CompileStatic Object getInput() {
            nativeReq.getInputStream()
        }
        @CompileStatic Object getOut() {
            nativeRes.getOutputStream()
        }
        @CompileStatic void write(Object data) {
            ((OutputStream)getOut()).write((byte[])data)
        }
        @CompileStatic Object getContextualizedInput(InputStream input) {
            input
        }
    }

}
