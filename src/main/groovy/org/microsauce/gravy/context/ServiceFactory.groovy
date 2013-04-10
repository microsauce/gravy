package org.microsauce.gravy.context

import groovy.transform.CompileStatic

import java.util.regex.Pattern

import javax.servlet.DispatcherType

import org.microsauce.gravy.module.Module
import org.microsauce.gravy.util.pattern.RegExUtils


class ServiceFactory {

    Module module

    ServiceFactory(Module module) {
        this.module = module
    }

    @CompileStatic
    public EnterpriseService makeEnterpriseService(Object scriptContext, String uriPattern, Map<String, Object> methodHandlers, List<DispatcherType> dispatch) {

        EnterpriseService service = new EnterpriseService()
        Map<String, Object> parseRoute = RegExUtils.parseRoute(uriPattern)

        HandlerFactory handlerFactory = HandlerFactory.getHandlerFactory(module.class.name)  // TODO use GravyType
        service.uriPattern = (Pattern) parseRoute.uriPattern
        service.uriString = uriPattern
        service.uriParamNames = parseRoute.params as List<String>
        service.dispatch = dispatch

        methodHandlers.each { String method, Object rawHandler ->
            Handler handler = handlerFactory.makeHandler(rawHandler, scriptContext)
            handler.module = module
            service.handlers.put(method, handler)
        }

        service
    }

    @CompileStatic
    CronService makeCronService(Object scriptContext, String cronString, Object rawHandler) {
        CronService cronService = new CronService()
        HandlerFactory handlerFactory = HandlerFactory.getHandlerFactory(module.class.name)
        cronService.cronString = cronString
        cronService.handlers['default'] = handlerFactory.makeHandler(rawHandler, scriptContext)
        cronService.module = module
        cronService
    }

}
