package org.microsauce.gravy.module

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.apache.log4j.ConsoleAppender
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.PatternLayout
import org.apache.log4j.PropertyConfigurator
import org.microsauce.gravy.lang.object.GravyType

import javax.servlet.DispatcherType
import javax.servlet.Filter
import javax.servlet.http.HttpServlet

import org.microsauce.gravy.context.Context
import org.microsauce.gravy.context.CronService
import org.microsauce.gravy.context.EnterpriseService
import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.HandlerFactory
import org.microsauce.gravy.context.ServiceFactory


@Log4j
abstract class Module {

    // module configuration
    ConfigObject moduleConfig
    ConfigObject applicationConfig
    ConfigObject config // the effective config: moduleConfig.merge applicationConfig

    // structural properties
    GravyType type
    String name
    Boolean isApp
    ClassLoader classLoader
    File folder
    File scriptFile
    File lib // TODO follow this through: module factory and any mod implementation that cares about it (Ruby)

    ServiceFactory serviceFactory

    // configurable properties - config.groovy
    String renderUri
    String errorUri

    Module app

    // the application context
    Context context
    Object scriptContext
    Logger moduleLogger

    @CompileStatic static List<String> moduleLoadOrder(Collection<String> availableModules, Object order) {
        List<String> moduleLoadOrder = new ArrayList<String>()
        if ( order && order instanceof List<String> ) {
            List<String> moduleOrder = (List<String>) order
            log.info "load modules in order: ${moduleOrder}"
            moduleOrder.each { String moduleName ->
                if ( availableModules.contains(moduleName) )
                    moduleLoadOrder << moduleName
            }
            if ( availableModules.size() > moduleLoadOrder.size() ) {
                Set<String> xor = new HashSet<String>(availableModules)
                xor.removeAll(moduleLoadOrder)
                xor.each { String moduleName ->
                    availableModules << moduleName
                }
            }
        } else {
            for (String thisModule in availableModules) {
                moduleLoadOrder << thisModule
            }
        }

        moduleLoadOrder.remove('app')   // app requires special handling
        moduleLoadOrder
    }

    Module() {}

    public getScriptContext() {
        return scriptContext;
    }

    @CompileStatic
    void load() {
        try {
            if (name == 'app')
                initLogging(config)

            doLoad()
        }
        catch (all) {
            log.error "failed to load module: ${name}", all
            all.printStackTrace()
            throw all
        }
    }

    @CompileStatic
    private void initLogging(ConfigObject config) {
        if (config.log4j) {
            PropertyConfigurator.configure(config.toProperties())
        } else {
            ConsoleAppender console = new ConsoleAppender()
            console.setLayout(new PatternLayout('%d{HH:mm:ss,SSS} [%p|%c] :: %m%n'))
            console.setThreshold(Level.DEBUG)
            console.setTarget('System.out')
            console.activateOptions()
            Logger microsauce = Logger.getLogger('org.microsauce')
            microsauce.setLevel(Level.ERROR)
            Logger.getRootLogger().removeAllAppenders()
            Logger.getRootLogger().addAppender(console)
            Logger.getRootLogger().setLevel(Level.DEBUG)
        }
    }

    /**
     *
     * @return module script return value
     */
    abstract protected Object doLoad()

    // TODO clean this up:
    // change 'default' to 'middleware'
    // check for method != 'middleware'
    static Set<String> END_POINTS = ['GET', 'POST', 'PUT', 'DELETE', 'get', 'post', 'put', 'delete']

    @CompileStatic
    public EnterpriseService addEnterpriseService(String uriPattern, String method, Object rawHandler) {
        log.info "addEnterpriseService: uri: $uriPattern - method: $method "

        EnterpriseService service = context.findServiceByUriString(uriPattern)
        if (service) {
            Handler thisHandler = HandlerFactory.getHandlerFactory(this.class.name).makeHandler(rawHandler, scriptContext)
            thisHandler.module = this
            thisHandler.service = service
            service.handlers[method] = thisHandler
        } else {
            Map<String, Object> methodHandler = [:]
            methodHandler[method] = rawHandler
            service = serviceFactory.makeEnterpriseService(scriptContext, uriPattern, methodHandler)
            service.module = this
        }
        service.endPoint = END_POINTS.contains(method)
        context.addEnterpriseService(service)
        service
    }

    @CompileStatic
    public void addEnterpriseService(String uriPattern, String method, List middleware, Object endPoint) {
        EnterpriseService endPointService = addEnterpriseService(uriPattern, method, endPoint)
        // add middleware handlers
        // TODO
        if ( middleware ) {
            middleware.each { Object rawHandler ->
                Handler thisHandler = HandlerFactory.getHandlerFactory(this.class.name).makeHandler(rawHandler, scriptContext)
                thisHandler.module = this
                thisHandler.service = endPointService
                endPointService.middleware << thisHandler
            }
        }
    }

    @CompileStatic
    public void addCronService(String cronPattern, Object rawHandler) {
        log.info "addCronService: cronPattern: $cronPattern"
        CronService service = serviceFactory.makeCronService(scriptContext, cronPattern, rawHandler)
        context.addCronService(service)
    }

    @CompileStatic void addParameterPrecondition(String param, Object rawHandler) {
        Map<String, Object> methodHandler = [:]
        methodHandler[EnterpriseService.MIDDLEWARE] = rawHandler
        EnterpriseService service = serviceFactory.makeEnterpriseService(scriptContext, '', methodHandler)
        service.module = this
        service.endPoint = false
        context.addParameterPrecondition(param, service)
    }
}
