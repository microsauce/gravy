package org.microsauce.gravy.dev.runtime

import javax.servlet.http.HttpServlet
import javax.servlet.Filter

import org.microsauce.gravy.runtime.FilterWrapper;
import org.microsauce.gravy.runtime.ServletWrapper;

//import org.microsauce.gravy.server.runtime.ServletWrapper;

abstract class ServerWrapper {

    def static final DEFAULT = 0
    def static final REQUEST = 1
    def static final FORWARD = 2
    def static final INCLUDE = 4
    def static final ERROR = 8
    def static final ASYNC = 16
    def static final ALL = 31


    List<ServletWrapper> servlets = []
    List<FilterWrapper> filters = []
    def protected config

    ServerWrapper(Map config) {
        this.config = config
    }

    void addServlet(ServletWrapper servlet) {
        servlets << servlet
    }

    void addFilter(FilterWrapper filter) {
        filters << filter
    }


    protected List<ServletWrapper> getServlets() {
        servlets
    }

    protected List<FilterWrapper> getFilters() {
        filters
    }

    abstract void initialize()

    abstract void start()

    abstract void stop()

}