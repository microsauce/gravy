package org.microsauce.gravy.server.bootstrap

import org.microsauce.gravy.app.*
import org.microsauce.gravy.app.config
import org.microsauce.gravy.server.runtime.*
import javax.servlet.*
import groovy.util.logging.Log4j
import org.apache.log4j.*
import org.microsauce.gravy.*


@Log4j
class GravyBootstrapListener implements ServletContextListener {

	private ApplicationContext app

	void contextDestroyed(ServletContextEvent sce) {
		log.info 'application is shutting down . . .'
		if (app) {
			app.reset()
			if (app.shutdown)
			  	app.shutdown()
		}
		log.info 'Shutdown complete.'
	}

	void contextInitialized(ServletContextEvent sce) {
		log.info('building application context . . .')
		def servletContext = sce.servletContext

		//
		// set appRoot
		//
		if (!System.getProperty('gravy.devMode')) 
			System.setProperty('gravy.appRoot', servletContext.getRealPath("/")+'/WEB-INF')

		//
		// load configuration
		//
		def environment = System.getProperty('gravy.env') ?: 'prod'
		def config = config.getInstance(environment).get()

		//
		// initialize logging
		//
		initLogging(config)

		//
		// initialize error handler
		//
		ErrorHandler.getInstance(config)

		//
		// decorate
		//
		GravyDecorator.decorateBinding()
		GravyDecorator.decorateHttpServletRequest()

		//
		// instantiate and build the Application Context
		//
		def appBuilder = new AppBuilder(config)
		def applicationContext = appBuilder.buildContext()

		//
		// start the source observer 
		//
		if (config.gravy.refresh) {
			def sourceObserver = new JNotifySourceModObserver(config) 
			sourceObserver.addScriptHandler(new RedeploySourceModHandler(config, applicationContext))

			sourceObserver.addCompiledSourceHandler(new BuildSourceModHandler(config, applicationContext))
			sourceObserver.addCompiledSourceHandler(new RedeploySourceModHandler(config, applicationContext))

			sourceObserver.start()
		}

		//
		// add services to the servlet context
		//
		ServletContext context = sce.servletContext
		applicationContext.getServlets().each { servlet ->
			def name = servlet.servlet.getClass().name
			if (name.lastIndexOf('.') > 0) {
			    name = name.substring(name.lastIndexOf('.')+1)
			}
			addServlet(name, servlet, context)
		}

		applicationContext.getFilters().each { filter ->
			def name = filter.filter.getClass().name
			if (name.lastIndexOf('.') > 0) {
			    name = name.substring(name.lastIndexOf('.')+1)
			}
			addFilter(name, filter, context)
		}

		addFilter('RouteFilter',new FilterWrapper([
			filter: new RouteFilter(), 
			mapping : '/*', 
			dispatch: EnumSet.copyOf([DispatcherType.REQUEST, DispatcherType.FORWARD])]), context) // TODO REVISIT
		addFilter('ControllerFilter',new FilterWrapper([
			filter: new ControllerFilter(), 
			mapping : '/*', 
			dispatch: EnumSet.copyOf([DispatcherType.REQUEST, DispatcherType.FORWARD])]), context)
	}

	private void addFilter(String name, FilterWrapper filter, ServletContext context) {
		def filterReg = context.addFilter(name, filter.filter)
		filterReg.addMappingForUrlPatterns(filter.dispatch, true, filter.mapping) 
	}

	private void addServlet(String name, ServletWrapper servlet, ServletContext context) {
		def servletReg = context.addServlet(name, servlet.servlet)
		servletReg.addMapping(servlet.mapping)
	}

	private void initLogging(config) {
		if (config.log4j) {
			PropertyConfigurator.configure(config.toProperties())
		}
	}


}