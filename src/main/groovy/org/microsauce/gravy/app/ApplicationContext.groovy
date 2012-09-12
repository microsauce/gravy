package org.microsauce.gravy.app

import groovy.transform.CompileStatic
import it.sauronsoftware.cron4j.Scheduler
import groovy.util.ConfigObject
import groovy.json.JsonBuilder
import javax.servlet.http.*
import javax.servlet.*
import org.microsauce.gravy.server.util.Mapper
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import groovy.util.logging.Log4j
import org.microsauce.gravy.server.runtime.FilterWrapper
import org.microsauce.gravy.server.runtime.ServletWrapper
import org.microsauce.gravy.server.util.*
import javax.servlet.DispatcherType


/**
* This type encapsulates and manages the application context defined
* in application.groovy
*/

@Log4j
class ApplicationContext extends ConfigObject {

	private static instance

	static ApplicationContext getInstance(ConfigObject config) {
		if ( !instance ) instance = new ApplicationContext(config)
		instance
	}
	static ApplicationContext getInstance() {
		if ( !instance ) throw new Exception('Application context not properly initialized')
		instance
	}

	def mapper = Mapper.getInstance()

	def private config

	//
	//
	//
	private List<Route> routes
	private Map<String, Controller> controllers
	def private tasks
	def private scheduler
	def private servlets = []
	def private filters = []

	def loadCache = [:]
	def modCache = [:]


	private ApplicationContext(config) {
		this.config=config
		initialize()
	}

//
// bootstrap API
//
	def getServlets() {
		servlets
	}

	def getFilters() {
		filters
	}

//
// script time API
//

	/**
	* Add a new controller to this instance of Gravy.
	*
	* @param name the controller name
	* @param actions a map of named actions (closures)
	*/
	def controller(String name, Map<String, Closure> actions) {
		addController(new Controller([name: name, actions: actions]))

		this
	}

	def route(String route, Closure action, Set<DispatcherType> dispatch ) {
		def routeData = parseRoute(route)

		def dipatches = EnumSet.copyOf(dispatch)
		routes << new Route([uriPattern: routeData.uriPattern, params: routeData.params, action: action, binding: null, dispatch: dipatches])

		this
	}

	def route(String route, Closure action) {
		def routeData = parseRoute(route)
		
		def dipatches = EnumSet.of(DispatcherType.REQUEST) 
		routes << new Route([uriPattern: routeData.uriPattern, params: routeData.params, action: action, binding: null, dispatch: dipatches])

		this
	}

	def route(String route, Class binding, Closure action) {
		def routeData = parseRoute(route)
		def dipatches = EnumSet.of(DispatcherType.REQUEST)
		routes << new Route([uriPattern: routeData.uriPattern, params: routeData.params, action: action, binding: binding, dispatch: dipatches])

		this
	}

	def route(String route, Class binding, Closure action, List dispatch) {
		def routeData = parseRoute(route)
		def dipatches = EnumSet.copyOf(dispatch)
		routes << new Route([uriPattern: routeData.uriPattern, params: routeData.params, action: action, binding: binding, dispatch: dipatches])

		this
	}

	def servlet(String mapping, HttpServlet servlet) { 
		servlets << new ServletWrapper([servlet: servlet, mapping: mapping])
		this	
	}

	def schedule(String cronString, Closure action) {
		addTask(new ScheduledTask([cronString: cronString, action: action]))

		this
	}

	def filter(String route, Filter filter) {
		def dipatches = EnumSet.of(DispactherType.REQUEST)
		filters << new FilterWrapper([filter: filter, mapping : route, dispatch: dipatches])

		this
	}

	def filter(String route, Filter filter, List dispatch ) {
		def dipatches = EnumSet.copyOf(dispatch)
		filters << new FilterWrapper([filter: filter, mapping : route, dispatch: dipatches])

		this
	}

	def private parseRoute(String pattern) {
		def paramPattern = /\/:([a-zA-Z]+)/
		def pathPattern = '\\/([_a-zA-Z0-9\\.]+)'

		def matches = pattern =~ paramPattern
		def params = [] as Set // return
		matches.each { group ->
			params << group[1]
		}
		def buffer = new StringBuffer()
		matches = pattern =~ paramPattern
		while (matches.find()) {
			def count = matches.groupCount()
			for (def i = 1; i <= count; i++) 
				matches.appendReplacement(buffer, pathPattern)
		}
		matches.appendTail(buffer)
		def newPattern = buffer.toString()
		def uriPattern = ~ newPattern

		[uriPattern: uriPattern, params: params]
	}
//
// runtime API
//
//

	@CompileStatic
	List<Route> findRoutes(String uri, DispatcherType dispatcherType) {
		def matchingRoutes = [] as List
		for ( Route route in routes ) {
			if ( matchDispatch(route, dispatcherType) 
				&& uri ==~ route.uriPattern ) matchingRoutes << route
		}
		matchingRoutes
	}

	@CompileStatic
	private boolean matchDispatch(Route route, DispatcherType dispatcherType) {
		route.dispatch.contains(dispatcherType)
	}

	@CompileStatic
	Controller findController(String controllerName) {
		controllers[controllerName]
	}

	void addController(Controller controller) {
		controllers[controller.name] = controller
	}

	void addTask(ScheduledTask task) {
		if (!scheduler) scheduler = new Scheduler()
		scheduler.schedule(task.cronString, task.action as Runnable)
	}

	void addServlet(servlet) {
		servlets << servlet
	}

	void addFilter(filter) {
		filters << filter
	}

	void complete() {
		log.info 'completing application context'
		makeControllers(this.entrySet(), new StringBuilder())

		if (scheduler) scheduler.start()
	}


	def reset() {
		log.info 'resetting application context'
		routes = []
		controllers = [:]
		tasks = []
		if (scheduler) {
			scheduler.stop()
			scheduler = null
		}
	}

	def initialize() {
		reset()
	}

	def private makeControllers(mapEntries, controllerName) {
		String thisControllerName = controllerName.toString()

		for (thisEntry in mapEntries) {

			def thisValue = thisEntry.value
			if (thisValue instanceof Map) {
				if (controllerName.length() == 0) {
					controllerName += thisEntry.key
				}
				else {
					controllerName += '/'+thisEntry.key
				}

				makeControllers(thisValue.entrySet(), controllerName) 
				controllerName = new String(thisControllerName)
			}
			else if (thisValue instanceof Closure) {
				def actionName = thisEntry.key
				def controller = findController(thisControllerName)
			
				if ( !controller ) {
					controller = new Controller([
						name:thisControllerName, 
						actions:[:]
					])
					addController(controller)
				}
				controller.actions[actionName] = thisValue
			}
		}
	}

} // class ApplicationContext
