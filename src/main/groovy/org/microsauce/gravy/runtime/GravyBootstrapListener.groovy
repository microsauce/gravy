package org.microsauce.gravy.runtime

import groovy.util.logging.Log4j

import javax.servlet.*

import org.apache.log4j.*
import org.microsauce.gravy.*
import org.microsauce.gravy.app.*
import org.microsauce.gravy.context.ApplicationContext
import org.microsauce.gravy.dev.observer.BuildSourceModHandler
import org.microsauce.gravy.dev.observer.JNotifySourceModObserver
import org.microsauce.gravy.dev.observer.RedeploySourceModHandler
import org.microsauce.gravy.module.AppBuilder
import org.microsauce.gravy.server.runtime.*
import org.microsauce.gravy.util.monkeypatch.groovy.GravyDecorator
import org.microsauce.gravy.module.config.Config


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
		def deployPath = servletContext.getRealPath("/")
		if (!System.getProperty('gravy.devMode')) 
			System.setProperty('gravy.appRoot', deployPath+'/WEB-INF')

		//
		// load configuration
		//
		def environment = System.getProperty('gravy.env') ?: 'prod'
		def config = Config.getInstance(environment).get()

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
		// configure resource paths
		//
		List<String> resourceRoots = []
		def mods = ['app']
		mods.addAll(config.gravy.modules)
		mods.each { mod ->
			log.info "adding folder $mod to the resource path"
			resourceRoots << deployPath+'/'+mod
			GravyTemplateServlet.roots << deployPath+'/WEB-INF/view/'+mod
		}

		//
		// instantiate and build the Application Context
		//
		def appBuilder = new AppBuilder(config)
		def applicationContext = appBuilder.buildContext()

		//
		// start the source observer 
		//
		if (config.gravy.refresh) {
			config.gravy.project = System.getProperty('user.dir')
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
		int serialNumber = 0
		applicationContext.getServlets().each { servlet ->
			def name = servlet.servlet.getClass().name
			if (name.lastIndexOf('.') > 0) {
			    name = name.substring(name.lastIndexOf('.')+1)
			}
			addServlet(name+serialNumber++, servlet, context)
		}

		serialNumber = 0
		applicationContext.getFilters().each { filter ->
			def name = filter.filter.getClass().name
			if (name.lastIndexOf('.') > 0) {
			    name = name.substring(name.lastIndexOf('.')+1)
			}
			addFilter(name+serialNumber++, filter, context)
		}

		addFilter('RouteFilter',new FilterWrapper([
			filter: new RouteFilter(), 
			mapping : '/*', 
			dispatch: EnumSet.copyOf([DispatcherType.REQUEST, DispatcherType.FORWARD])]), context) // TODO REVISIT
		addFilter('ControllerFilter',new FilterWrapper([
			filter: new ControllerFilter(), 
			mapping : '/*', 
			dispatch: EnumSet.copyOf([DispatcherType.REQUEST, DispatcherType.FORWARD])]), context)

		addFilter('GravyResourceFilter',new FilterWrapper([
			filter: new GravyResourceFilter(resourceRoots, deployPath), 
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