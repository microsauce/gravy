package org.microsauce.gravy.context

import groovy.transform.CompileStatic

import javax.servlet.DispatcherType

import org.microsauce.gravy.module.Module

abstract class Service {

    static String GET = 'get'
    static String POST = 'post'
    static String PUT = 'put'
    static String DELETE = 'delete'
    static String DEFAULT = 'default'

    Module module
    Class binding
    List<String> uriParamNames
    List<DispatcherType> dispatch = []

    Map<String, Handler> handlers = [:]
    HandlerFactory handlerFactory
    String viewUri

    @CompileStatic void setHandler(String method, Object rawHandler, Object scriptContext) {
        Handler handler = handlerFactory.makeHandler rawHandler, scriptContext
        handlers[method] = handler
    }
}
