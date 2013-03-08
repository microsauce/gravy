package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic

import javax.servlet.http.HttpSession

import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.lang.patch.BaseEnterpriseProxy
import org.microsauce.gravy.module.Module

class GravySessionProxy<T extends HttpSession> extends BaseEnterpriseProxy {

    Module module

    GravySessionProxy(Object target, Module module) {
        super(target)
        this.module = module
    }

    @CompileStatic
    Object get(String key) {
        CommonObject obj = (CommonObject) ((T) target).getAttribute(key)
        obj.value(context())
    }

    @CompileStatic
    void put(String key, Object value) {
        CommonObject obj = new CommonObject(value, context())
        ((T) target).setAttribute key, obj
    }

    @CompileStatic
    protected Module context() {
        module
    }

} 

