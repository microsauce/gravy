

package org.microsauce.gravy.runtime

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import it.sauronsoftware.cron4j.Scheduler

import javax.servlet.*

import org.apache.log4j.*
import org.microsauce.gravy.*
import org.microsauce.gravy.app.*
import org.microsauce.gravy.context.Context
import org.microsauce.gravy.context.CronService
import org.microsauce.gravy.context.Handler
import org.microsauce.gravy.dev.observer.BuildSourceModHandler
import org.microsauce.gravy.dev.observer.JNotifySourceModObserver
import org.microsauce.gravy.dev.observer.RedeploySourceModHandler
import org.microsauce.gravy.module.ContextBuilder
import org.microsauce.gravy.module.Module
import org.microsauce.gravy.module.ModuleFactory
import org.microsauce.gravy.server.runtime.*


@Log4j
class GravyBootstrapListener implements ServletContextListener {

	private Context context

	@CompileStatic
	void contextDestroyed(ServletContextEvent sce) {
		log.info 'application is shutting down . . .'
		if ( context.cronScheduler ) {
			if (context.cronScheduler.started)
			  	context.cronScheduler.stop()
		}
		log.info 'Shutdown complete.'
	}

	void contextInitialized(ServletContextEvent sce) {
		log.info('building application context . . .')
		def servletContext = sce.servletContext

		//
		// set gravy system properties
		//
		String appRoot = servletContext.getRealPath("/")
		String moduleRoot = appRoot+'/WEB-INF/modules'

		System.setProperty('gravy.appRoot', appRoot)
		System.setProperty('gravy.moduleRoot', moduleRoot)
		System.setProperty('gravy.viewRoot', appRoot+'/WEB-INF/view')

		//
		// load configuration
		//
		String environment = System.getProperty('gravy.env') ?: 'prod'
		ConfigObject config = ModuleFactory.loadModuleConfig(new File("${appRoot}/modules/app"), environment) //Config.getInstance(environment).get()

		//
		// initialize logging
		//
		initLogging(config)

		//
		// initialize error handler
		//
		String errorPage = config.gravy.errorPage ?: null
		String viewUri = config.gravy.viewUri ?: null


		//
		// configure resource paths
		//
		File moduleRootFolder = new File(moduleRoot) 
		List<String> resourceRoots = []
		for ( File modFolder in ContextBuilder.listAllModules(moduleRootFolder) ) {
			log.info "adding folder ${modFolder.name} to the resource path"
			String moduleName = modFolder.name
			String moduleResoursesFolder = "${appRoot}/${moduleName}".toString()
			if ( new File(moduleResoursesFolder).exists() )
				resourceRoots << moduleResoursesFolder
			String moduleViewFolder = "${appRoot}/WEB-INF/view/${moduleName}".toString()
		}

		//
		// instantiate and build the Application Context
		//
		ContextBuilder contextBuilder = new ContextBuilder(new File(appRoot), environment)
		contextBuilder.build()
		Context context = contextBuilder.context
		this.context = context
		Module app = contextBuilder.application

		if (config.gravy.refresh) {
			startSourceObserver app
		}
					
		initEnterpriseRuntime context, resourceRoots, appRoot, sce, config.gravy.view.errorUri
		initCronRuntime context
		
	}

	@CompileStatic private void initCronRuntime(Context context) {
		if ( context.cronServices ) {
			context.cronScheduler = new Scheduler()
			context.cronServices.each { CronService service ->
				Closure runnable = { 
					Handler handler = service.handlers['default']
					handler.execute()
				}
				context.cronScheduler.schedule(service.cronString, runnable as Runnable)
			}

			context.cronScheduler.start()
		}
	}
	
	@CompileStatic
	private void startSourceObserver(Module app) {
		Map binding = app.imports
		String projectFolder = System.getProperty('user.dir')
		def sourceObserver = new JNotifySourceModObserver(projectFolder) 
		sourceObserver.addScriptHandler(new RedeploySourceModHandler(app))

		sourceObserver.addCompiledSourceHandler(new BuildSourceModHandler())
		sourceObserver.addCompiledSourceHandler(new RedeploySourceModHandler(app))

		sourceObserver.start()

	}

	
	private void initEnterpriseRuntime(Context context, List<String> resourceRoots, String deployPath, ServletContextEvent sce, String errorUri) {
		ServletContext servletContext = sce.servletContext
		int serialNumber = 0
		context.servlets.each { servlet ->
			def name = servlet.servlet.getClass().name
			if (name.lastIndexOf('.') > 0) {
				name = name.substring(name.lastIndexOf('.')+1)
			}
			addServlet(name+serialNumber++, servlet, servletContext)
		}

		serialNumber = 0
		context.filters.each { filter ->
			def name = filter.filter.getClass().name
			if (name.lastIndexOf('.') > 0) {
				name = name.substring(name.lastIndexOf('.')+1)
			}
			addFilter(name+serialNumber++, filter, servletContext)
		}

		addFilter('RouteFilter',new FilterWrapper([
			filter: new RouteFilter(context, errorUri),
			mapping : '/*',
			dispatch: EnumSet.copyOf([DispatcherType.REQUEST, DispatcherType.FORWARD])]), servletContext) 
		addFilter('GravyResourceFilter',new FilterWrapper([
			filter: new GravyResourceFilter(resourceRoots, deployPath),
			mapping : '/*',
			dispatch: EnumSet.copyOf([DispatcherType.REQUEST, DispatcherType.FORWARD])]), servletContext)

	}
	
	
	private void addFilter(String name, FilterWrapper filter, ServletContext context) {
		def filterReg = context.addFilter(name, filter.filter)
		filterReg.addMappingForUrlPatterns(filter.dispatch, true, filter.mapping) 
	}

	private void addServlet(String name, ServletWrapper servlet, ServletContext context) {
		def servletReg = context.addServlet(name, servlet.servlet)
		servletReg.addMapping(servlet.mapping)
	}

	@CompileStatic
	private void initLogging(ConfigObject config) {
		if (config.log4j) {
			PropertyConfigurator.configure(config.toProperties())
		}
	}

}