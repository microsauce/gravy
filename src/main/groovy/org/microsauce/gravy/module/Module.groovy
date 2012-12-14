package org.microsauce.gravy.module

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j;

import javax.servlet.DispatcherType
import javax.servlet.Filter
import javax.servlet.http.HttpServlet

import org.microsauce.gravy.context.Context
import org.microsauce.gravy.context.CronService
import org.microsauce.gravy.context.EnterpriseService
import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.context.ServiceFactory
import org.microsauce.gravy.runtime.GravyTemplateServlet

@Log4j
abstract class Module { 
	
	ConfigObject moduleConfig
	ConfigObject applicationConfig
	ConfigObject config // the effective config: moduleConfig.merge applicationConfig
	
	String name 
	Boolean isApp
	ClassLoader classLoader 
	File folder 
	File scriptFile
	ServiceFactory serviceFactory
	Map<String, Map<String, Object>> rawServiceMap = [:]
	String renderUri
	List<String> viewRoots
	String errorUri
	boolean serializeAttributes // TODO global config option

	Object returnValue
	Map binding // used by the main app script only (mod1 ret val, mod2, etc)
	
	// pass these into the scripting environment
	Context context //
	Object scriptContext
	
	Module() {}
	
	@CompileStatic void load() {

		try {
			if ( binding == null ) binding = [:]
			returnValue = doLoad(binding) 
		}
		catch ( all ) {
			log.error "failed to load module: ${name}", all
			all.printStackTrace()
			throw all
		}
	}
	
	/**
	 * 
	 * @return module script return value
	 */
	abstract protected Object doLoad(Map binding)

	@CompileStatic public void addEnterpriseService(String uriPattern, String method, Object rawHandler, List<DispatcherType> dispatch) {
		log.info "addEnterpriseService: uri: $uriPattern - method: $method - dispatch: $dispatch"
		EnterpriseService service = context.findServiceByUriString(uriPattern)
		if ( service ) {
			Handler thisHandler = service.handlerFactory.makeHandler(rawHandler, scriptContext)
//			thisHandler.viewUri = viewUri
			thisHandler.module = this
			service.handlers[method] = thisHandler
		} else {
			Map<String, Object> methodHandler = [:]
			methodHandler[method] = rawHandler
			service = serviceFactory.makeEnterpriseService(scriptContext, uriPattern, methodHandler, dispatch, this)
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
