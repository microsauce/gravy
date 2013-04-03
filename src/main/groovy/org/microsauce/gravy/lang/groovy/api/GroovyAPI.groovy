package org.microsauce.gravy.lang.groovy.api

import static org.microsauce.gravy.context.EnterpriseService.DEFAULT
import static org.microsauce.gravy.context.EnterpriseService.DELETE
import static org.microsauce.gravy.context.EnterpriseService.GET
import static org.microsauce.gravy.context.EnterpriseService.OPTIONS
import static org.microsauce.gravy.context.EnterpriseService.POST
import static org.microsauce.gravy.context.EnterpriseService.PUT

import javax.servlet.DispatcherType
import javax.servlet.Filter
import javax.servlet.http.HttpServlet

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

    static void get(String uriPattern, Closure handler) {
        module.addEnterpriseService(uriPattern, GET, handler, [REQUEST, FORWARD])
    }

    static void post(String uriPattern, Closure handler) {
        module.addEnterpriseService(uriPattern, POST, handler, [REQUEST, FORWARD])
    }

    static void put(String uriPattern, Closure handler) {
        module.addEnterpriseService(uriPattern, PUT, handler, [REQUEST, FORWARD])
    }

    static void delete(String uriPattern, Closure handler) {
        module.addEnterpriseService(uriPattern, DELETE, handler, [REQUEST, FORWARD])
    }

    static void options(String uriPattern, Closure handler) {
        module.addEnterpriseService(uriPattern, OPTIONS, handler, [REQUEST, FORWARD])
    }

    static void route(String uriPattern, Closure handler) {
        module.addEnterpriseService(uriPattern, DEFAULT, handler, [REQUEST, FORWARD])
    }

    static void use(String uriPattern, Closure handler) {
        module.addEnterpriseService(uriPattern, DEFAULT, handler, [REQUEST, FORWARD])
    }

    static GroovyAPI.Route route(String uriPattern) {
        GroovyAPI.Route route = new GroovyAPI.Route()
        route.uriPattern = uriPattern
        route.dispatch = [REQUEST]
        ROUTES << route
        route
    }

    static void get(String uriPattern, List<DispatcherType> dispatch, Closure handler) {
        module.addEnterpriseService(uriPattern, GET, handler, dispatch)
    }

    static void post(String uriPattern, List<DispatcherType> dispatch, Closure handler) {
        module.addEnterpriseService(uriPattern, POST, handler, dispatch)
    }

    static void put(String uriPattern, List<DispatcherType> dispatch, Closure handler) {
        module.addEnterpriseService(uriPattern, PUT, handler, dispatch)
    }

    static void delete(String uriPattern, List<DispatcherType> dispatch, Closure handler) {
        module.addEnterpriseService(uriPattern, DELETE, handler, dispatch)
    }

    static void route(String uriPattern, List<DispatcherType> dispatch, Closure handler) {
        module.addEnterpriseService(uriPattern, DEFAULT, handler, dispatch)
    }

    static void use(String uriPattern, List<DispatcherType> dispatch, Closure handler) {
        module.addEnterpriseService(uriPattern, DEFAULT, handler, dispatch)
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

    static void servlet(String mapping, HttpServlet servlet) {
        module.addServlet(mapping, servlet)
    }

    static void filter(String uriPattern, Filter filter) {
        module.addFilter(uriPattern, filter)
    }

    static void filter(String uriPattern, List<DispatcherType> dispatch, Filter filter) {
        module.addFilter(uriPattern, filter, dispatch)
    }

    static void complete() {
        // TODO handle exports

        ROUTES.each { GroovyAPI.Route route ->
            if (route.get)
                module.addEnterpriseService(route.uriPattern, GET, route.get, route.dispatch)
            if (route.post)
                module.addEnterpriseService(route.uriPattern, POST, route.post, route.dispatch)
            if (route.put)
                module.addEnterpriseService(route.uriPattern, PUT, route.put, route.dispatch)
            if (route.options)
                module.addEnterpriseService(route.uriPattern, OPTIONS, route.options, route.dispatch)
            if (route.delete)
                module.addEnterpriseService(route.uriPattern, DELETE, route.delete, route.dispatch)
            if (route.route)
                module.addEnterpriseService(route.uriPattern, DEFAULT, route.route, route.dispatch)
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
                module.addEnterpriseService(uri.toString(), DEFAULT, thisValue, [REQUEST, FORWARD])
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
        Closure options
        Closure route
    }

}