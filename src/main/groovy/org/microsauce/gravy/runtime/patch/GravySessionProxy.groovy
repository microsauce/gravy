package org.microsauce.gravy.runtime.patch

import groovy.transform.CompileStatic

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import org.microsauce.gravy.lang.object.CommonObject
import org.microsauce.gravy.lang.object.GravyType
import org.microsauce.gravy.lang.patch.BaseEnterpriseProxy
import org.microsauce.gravy.module.Module

class GravySessionProxy<T extends HttpSession> extends BaseEnterpriseProxy {

    GravySessionProxy(Object target) {
        super(target)
    }

    @CompileStatic
    Object get(String key) {
        CommonObject obj = (CommonObject) ((T) target).getAttribute(key)
        Module module = ((T)target).getAttribute('_module') as Module
        obj.value(module)
    }

    @CompileStatic
    void put(String key, Object value) {
        Module module = ((T)target).getAttribute('_module') as Module
        CommonObject obj = new CommonObject(value, module)
        ((T) target).setAttribute key, obj
    }

} 

