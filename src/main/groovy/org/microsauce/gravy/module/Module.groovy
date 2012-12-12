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
import org.microsauce.gravy.runtime.ErrorHandler
import org.microsauce.gravy.runtime.GravyTemplateServlet

@Log4j
abstract class Module { 
	
	// TODO add viewUri - from config
	String name 
	Boolean isApp
	ClassLoader classLoader 
	File folder 
	File scriptFile
	ConfigObject moduleConfig
	ConfigObject applicationConfig
	ConfigObject config // the effective config
	ServiceFactory serviceFactory
	Map<String, Map<String, Object>> rawServiceMap = [:]
	String viewUri
	List<String> viewRoots
	ErrorHandler errorHandler 

	Object returnValue
	Map binding // used by the main app script only (mod1 ret val, mod2, etc)
	
	// pass these into the scripting environment
	Context context //
	Object scriptContext
	
	Module() {}
	
//	Module(String name, File folder, ConfigObject moduleConfig, ConfigObject applicationConfig, Map bindings) {
//		// TODO classLoader = new ModuleClassLoader() // extends Rootloader
//		// TODO don't forget to add the merged config to the binding (grooyv)
//		this.serviceFactory = ServiceFactory.getFactory(this.class) // TODO verify
//		this.name = name
//		this.folder = folder
//		this.moduleConfig = moduleConfig
//		this.applicationConfig = applicationConfig
//		this.bindings = bindings
//	}
	
	@CompileStatic
	void load() {
		GravyTemplateServlet.roots.addAll(viewRoots) 
		
		try {
			if (binding == null) binding = [:]
			returnValue = doLoad(binding) // TODO: verify merge
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

	@Override
	@CompileStatic
	public void addEnterpriseService(String uriPattern, String method, Object rawHandler, List<DispatcherType> dispatch) {
		EnterpriseService service = context.findServiceByUriString(uriPattern)
		if ( service ) {
			Handler thisHandler = service.handlerFactory.makeHandler(rawHandler, scriptContext)
			thisHandler.viewUri = viewUri
			thisHandler.module = this
			service.handlers[method] = thisHandler
		} else {
			Map<String, Object> methodHandler = [:]
			methodHandler[method] = rawHandler
			service = serviceFactory.makeEnterpriseService(scriptContext, uriPattern, methodHandler, dispatch, this)
			service.errorHandler = errorHandler
			service.module = this
		}
		
		context.addEnterpriseService(service)
	}

	@Override
	public void addCronService(String cronPattern, Object rawHandler) {
		CronService service = serviceFactory.makeCronService(scriptContext, cronPattern, rawHandler)
		context.addCronService(service)
	}
	
	public void addServlet(String mapping, HttpServlet servlet) {
		context.addServlet(mapping, servlet)
	}
	
	public void addFilter(String uriPatter, Filter servlet) {
		context.addFilter(uriPatter, servlet)
	}

	public void addFilter(String uriPatter, List<DispatcherType> dispatch, Filter servlet) {
		context.addFilter(uriPatter, servlet)
	}
}
