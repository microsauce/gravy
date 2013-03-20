package org.microsauce.gravy.context

import groovy.transform.CompileStatic
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.runtime.patch.GravyHttpServletRequest
import org.microsauce.gravy.runtime.patch.GravyHttpServletResponse
import org.microsauce.gravy.runtime.patch.GravyRequestProxy
import org.microsauce.gravy.runtime.patch.GravyResponseProxy
import org.microsauce.gravy.runtime.patch.ServletWrapper
import org.microsauce.gravy.runtime.patch.ServletWrapperFactory

import javax.servlet.FilterChain
import javax.servlet.ServletException
import java.util.regex.Matcher
import java.util.regex.Pattern

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

// TODO add Gravy Req, Res, Sess Proxy properties (initializers will remain in Handler and subclasses)
// TODO add getReq(GravyType) getRes(GravyType) methods
class GravyServletWrapper {

    HttpServletRequest nativeReq
    HttpServletResponse nativeRes
    FilterChain nativeChain

    Pattern uriPattern
    String requestUri

    Map<String, String> paramMap = [:]      // uri named parameter map
    List<String> paramList = []             // complete uri parameter value list
    List<String> splat = []                 // wild card 'splat' list
    CommonObject json                       // a json payload

    Map<String,Object> params

    Map<String, ServletWrapper> commonServlet = new HashMap<String,ServletWrapper>();

    GravyServletWrapper(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Pattern uriPattern, List<String> params) {
        init(req, res, chain, uriPattern, params)
    }

    @CompileStatic GravyHttpServletRequest getReq(GravyType gravyType) {
        ServletWrapper servlet = commonServlet[gravyType.type]
        if ( !servlet ) {
            Module module = nativeReq.getAttribute('_module') as Module
            servlet = initServlet(module.type)
            commonServlet[gravyType.type] = servlet
        }
        servlet.getReq()
    }

    @CompileStatic GravyHttpServletResponse getRes(GravyType gravyType) {
        ServletWrapper servlet = commonServlet[gravyType.type]
        if ( !servlet ) {
            servlet = initServlet(gravyType)
            commonServlet[gravyType.type] = servlet
        }
        servlet.getRes()
    }

    @CompileStatic ServletWrapper initServlet(GravyType type) {
        ServletWrapperFactory.getWrapper(type, nativeReq, nativeRes, nativeChain)
    }

    @CompileStatic
    void init(HttpServletRequest req, HttpServletResponse res, FilterChain chain, Pattern uriPattern, List<String> paramNames) {

        this.nativeReq = req
        this.nativeRes = res
        this.nativeChain = chain

        this.requestUri = req.requestURI
        this.uriPattern = uriPattern

        json = jsonReq(req)
        this.params = retrieveParameters(req)

        Matcher matches = requestUri =~ uriPattern
        Integer ndx = 1
        splat = []
        if (paramNames.size() > 0) {
            while (matches.find()) {
                Integer groupCount = matches.groupCount()
                for (; ndx <= groupCount; ndx++) {
                    String thisParamName = paramNames[ndx - 1]
                    String paramValue = matches.group(ndx)
                    paramList << paramValue
                    if (thisParamName == '*')
                        splat << paramValue
                    else {
                        paramMap[thisParamName] = paramValue
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

    @CompileStatic
    private String readJsonPayload(HttpServletRequest req) {
        String json = null
        if (req.contentType && req.contentType.startsWith('application/json')) {
            json = req.inputStream.getText('UTF-8')
        }
        json
    }

    @CompileStatic private CommonObject jsonReq(HttpServletRequest req) {
        String payload = readJsonPayload(req)
        CommonObject json = null
        if ( payload ) {
            Module module = req.getAttribute('_module') as Module
            json = new CommonObject(null, module)
            json.serializedRepresentation = payload
        }
        json
    }

    @CompileStatic private Map retrieveParameters(HttpServletRequest req) {
        Map parsedRequest = [:]
        req.setAttribute('_req_parsed', parsedRequest)
        if ((req.method == 'POST' || req.method == 'PUT') && ServletFileUpload.isMultipartContent(req)) {
            try {
                List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(req); // TODO add config options
                for (item in items) {
                    if (item.isFormField()) parsedRequest[item.getFieldName()] = item.getString()
                    else {
                        Module module = req.getAttribute('_module') as Module
                        parsedRequest[item.getFieldName()] = module.wrapInputStream(item.getInputStream())
                    }
                }
            } catch (FileUploadException e) {
                throw new ServletException("Failed to parse multi-part request.", e);
            }
        } else {
            req.getParameterNames().each { String key ->
                parsedRequest[key] = req.getParameter(key)
            }
        }
        return parsedRequest
    }

}
