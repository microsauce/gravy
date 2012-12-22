package org.microsauce.gravy.context

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import it.sauronsoftware.cron4j.Scheduler

import javax.servlet.DispatcherType
import javax.servlet.Filter
import javax.servlet.http.HttpServlet

import org.microsauce.gravy.runtime.FilterWrapper
import org.microsauce.gravy.runtime.ServletWrapper


/**
 * 
 * @author jboone
 *
 */
@Log4j
class Context {
	
	Scheduler cronScheduler

	List<EnterpriseService> enterpriseServices = []
	List<CronService> cronServices = []

	List<ServletWrapper> servlets = []
	List<Filter> filters = []
	
	@CompileStatic
	void addEnterpriseService(EnterpriseService service) {
		enterpriseServices << service
	}
	
	@CompileStatic
	void addCronService(CronService service) {
		cronServices << service
	}
	
	void addServlet(String mapping, HttpServlet servlet) {
		servlets << new ServletWrapper([servlet: servlet, mapping: mapping])
	}
	
	void addFilter(String uriPattern, Filter filter) {
		def dipatches = EnumSet.of(DispactherType.REQUEST)
		filters << new FilterWrapper([filter: filter, mapping : uriPattern, dispatch: dipatches])
	}

	void addFilter(String route, Filter filter, List dispatch) {
		def dipatches = EnumSet.copyOf(dispatch)
		filters << new FilterWrapper([filter: filter, mapping : route, dispatch: dipatches])
	}

	/**
	 * This is a dev mode convenience.  Clear all app module services 
	 * (prior to re-building the application context).
	 */
	@CompileStatic
	void clearApplicationServices() {
		clearAppServices enterpriseServices
		clearAppServices cronServices
	}
	
	@CompileStatic
	private void clearAppServices(List<? extends Service> services) {
		List<Service> clearList = []
		services.each { Service service ->
			if ( service.module.isApp )
				clearList << service
		}
		
		services.removeAll(clearList)
	}
	
	@CompileStatic
	List<EnterpriseService> findService(String uri, DispatcherType dispatcherType) {
		List<EnterpriseService> matchingServices = [] as List
		for ( EnterpriseService service in enterpriseServices ) {
			if ( service.dispatch.contains(dispatcherType)
				&& uri ==~ service.uriPattern ) matchingServices << service
		}
		matchingServices
	}
	
	@CompileStatic
	EnterpriseService findServiceByUriString(String uriString) {
		EnterpriseService retService = null
		enterpriseServices.each { EnterpriseService service ->
			if ( service.uriString == uriString  )	retService = service  // there will be only one	
		}
		
		retService
	}

}
