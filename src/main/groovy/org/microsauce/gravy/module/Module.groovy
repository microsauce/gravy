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
	protected File folder 
	File scriptFile
	File lib // TODO follow this through: module factory and any mod implementation that cares about it (Ruby)
	
	ServiceFactory serviceFactory
	
	// configurable properties - config.groovy
	String renderUri
	String errorUri
	 
	Module app // TODO flesh this out contextbuilder and any mod implementation that cares about it

	Object exports // TODO refactor as exports
	Map imports // TODO refactor as imports
	
	// the application context
	Context context 
	Object scriptContext
    Logger moduleLogger

	
	Module() {}
	
	public getScriptContext() {
		return scriptContext;
	}
	
	@CompileStatic void load() {
		try {
			if ( imports == null ) imports = [:]
			exports = doLoad(imports)
            if ( name == 'app' )
                //
                // initialize logging
                //
                initLogging(config) // TODO move this to Module - only executed for 'app'

        }
		catch ( all ) {
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
            microsauce.setLevel(Level.OFF)
            Logger.getRootLogger().removeAllAppenders()
            Logger.getRootLogger().addAppender(console)
            Logger.getRootLogger().setLevel(Level.DEBUG)
        }
    }


    /**
	 * 
	 * @return module script return value
	 */
	abstract protected Object doLoad(Map imports)

	@CompileStatic public void addEnterpriseService(String uriPattern, String method, Object rawHandler, List<DispatcherType> dispatch) {
		log.info "addEnterpriseService: uri: $uriPattern - method: $method - dispatch: $dispatch"
	
		EnterpriseService service = context.findServiceByUriString(uriPattern)
		if ( service ) {
			Handler thisHandler = HandlerFactory.getHandlerFactory(this.class.name).makeHandler(rawHandler, scriptContext)
			thisHandler.module = this
			service.handlers[method] = thisHandler
		} else {
			Map<String, Object> methodHandler = [:]
			methodHandler[method] = rawHandler
			service = serviceFactory.makeEnterpriseService(scriptContext, uriPattern, methodHandler, dispatch)
			service.module = this
		}
		
		context.addEnterpriseService(service)
	}

	@CompileStatic public void addCronService(String cronPattern, Object rawHandler) {
		log.info "addCronService: cronPattern: $cronPattern"
		CronService service = serviceFactory.makeCronService(scriptContext, cronPattern, rawHandler)
		context.addCronService(service)
	}
	
	@CompileStatic public void addServlet(String mapping, HttpServlet servlet) {
		log.info "addServlet: mapping: $mapping"
		context.addServlet(mapping, servlet)
	}
	
	@CompileStatic public void addFilter(String uriPattern, Filter servlet) {
		log.info "addFilter: uriPattern: $uriPattern"
		context.addFilter(uriPattern, servlet)
	}

	@CompileStatic public void addFilter(String uriPattern, List<DispatcherType> dispatch, Filter servlet) {
		log.info "addFilter: uriPattern: $uriPattern - dispatch: $dispatch"
		context.addFilter(uriPattern, servlet)
	}
}
