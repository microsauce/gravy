package org.microsauce.gravy.lang.groovy.api


import static org.microsauce.gravy.context.EnterpriseService.DELETE
import static org.microsauce.gravy.context.EnterpriseService.GET
import static org.microsauce.gravy.context.EnterpriseService.MIDDLEWARE
import static org.microsauce.gravy.context.EnterpriseService.POST
import static org.microsauce.gravy.context.EnterpriseService.PUT

import javax.servlet.DispatcherType

import org.microsauce.gravy.module.Module

/**
 *
 */
class GroovyAPI {

    static Module module
    static ConfigObject root

    static DispatcherType REQUEST = DispatcherType.REQUEST
    static DispatcherType FORWARD = DispatcherType.FORWARD

    static List<GroovyAPI.Route> ROUTES = []

    static void get(String uriPattern, Closure ... handlers) {
        def handlerList = handlers as List
        def middleware = []
        if ( handlerList.size() > 1 )
            middleware.addAll(handlerList.subList(0,handlerList.size()-1))
        def endPoint = handlers[handlers.size()-1]
        module.addEnterpriseService(uriPattern, GET, middleware, endPoint)
    }

    static void post(String uriPattern, Closure ... handlers) {
        def handlerList = handlers as List
        def middleware = []
        if ( handlerList.size() > 1 )
            middleware.addAll(handlerList.subList(0,handlerList.size()-1))
        def endPoint = handlers[handlers.size()-1]
        module.addEnterpriseService(uriPattern, POST, middleware, endPoint)
    }

    static void put(String uriPattern, Closure ... handlers) {
        def handlerList = handlers as List
        def middleware = []
        if ( handlerList.size() > 1 )
            middleware.addAll(handlerList.subList(0,handlerList.size()-1))
        def endPoint = handlers[handlers.size()-1]
        module.addEnterpriseService(uriPattern, PUT, middleware, endPoint)
    }

    static void delete(String uriPattern, Closure ... handlers) {
        def handlerList = handlers as List
        def middleware = []
        if ( handlerList.size() > 1 )
            middleware.addAll(handlerList.subList(0,handlerList.size()-1))
        def endPoint = handlers[handlers.size()-1]
        module.addEnterpriseService(uriPattern, DELETE, middleware, endPoint)
    }

    static void all(String uriPattern, Closure ... handlers) {
        get(uriPattern, handlers)
        post(uriPattern, handlers)
        put(uriPattern, handlers)
        delete(uriPattern, handlers)
    }

    static void route(String uriPattern, Closure ... handlers) {
        def handlerList = handlers as List
        def middleware = []
        if ( handlerList.size() > 1 )
            middleware.addAll(handlerList.subList(0,handlerList.size()-1))
        def endPoint = handlers[handlers.size()-1]
        module.addEnterpriseService(uriPattern, MIDDLEWARE, middleware, endPoint)
    }

    static void route(Closure ... handlers) {
        route('/*', handlers)
    }

    static void use(String uriPattern, Closure ... handlers) {
        route(uriPattern, handlers)
    }

    static void use(Closure ... handlers) {
        route('/*', handlers)
    }

    static void param(String param, Closure handler) {
        module.addParameterPrecondition(param, handler)
    }

    static GroovyAPI.Route route(String uriPattern) {
        GroovyAPI.Route route = new GroovyAPI.Route()
        route.uriPattern = uriPattern
        route.dispatch = [REQUEST]
        ROUTES << route
        route
    }

    static GroovyAPI.Route route(String uriPattern, List<DispatcherType> dispatch) {
        GroovyAPI.Route route = new GroovyAPI.Route()
        route.uriPattern = uriPattern
        route.dispatch = dispatch
        ROUTES << route
        route
    }

    static void schedule(String cronString, Closure handler) {
        module.addCronService(cronString, handler)
    }

    static void complete() {

        ROUTES.each { GroovyAPI.Route route ->
            if (route.get)
                module.addEnterpriseService(route.uriPattern, GET, route.get)
            if (route.post)
                module.addEnterpriseService(route.uriPattern, POST, route.post)
            if (route.put)
                module.addEnterpriseService(route.uriPattern, PUT, route.put)
            if (route.delete)
                module.addEnterpriseService(route.uriPattern, DELETE, route.delete)
            if (route.route)
                module.addEnterpriseService(route.uriPattern, MIDDLEWARE, route.route)
        }

        traverseUriTree(root.entrySet, new StringBuilder())
    }

    private static void traverseUriTree(Set<Map.Entry> mapEntries, StringBuilder uri) {

        String dummy = uri.toString()

        for (Map.Entry thisEntry in mapEntries) {

            Object thisValue = thisEntry.value
            if (thisValue instanceof Map) {
                uri += '/' + thisEntry.key

                traverseUriTree(((Map) thisValue).entrySet(), uri)
                uri = new StringBuilder(dummy)
            } else if (thisValue instanceof Closure) {
                uri += (String) thisEntry.key
                module.addEnterpriseService(uri.toString(), MIDDLEWARE, thisValue, [REQUEST, FORWARD])
            }
        }
    }


    static class Route {
        String uriPattern
        List<DispatcherType> dispatch
        Closure get
        Closure post
        Closure put
        Closure delete
        Closure route
    }

}