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
    Map<String, EnterpriseService> paramServices = [:]
    List<CronService> cronServices = []

    @CompileStatic
    void addEnterpriseService(EnterpriseService service) {
        enterpriseServices << service
    }

    @CompileStatic
    void addParameterPrecondition(String parameterName, EnterpriseService service) {
        paramServices[parameterName] = service
    }

    @CompileStatic
    void addCronService(CronService service) {
        cronServices << service
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
            if (service.module.isApp)
                clearList << service
        }

        services.removeAll(clearList)
    }

    @CompileStatic
    List<EnterpriseService> findService(String uri, DispatcherType dispatcherType) {

        List<EnterpriseService> matchingServices = [] as List
        for (EnterpriseService service in enterpriseServices) {
            if (service.dispatch.contains(dispatcherType) // TODO drop dispatcherType
                    && uri ==~ service.uriPattern) {
                matchingServices << service
                if ( service.endPoint ) break
            }
        }
        matchingServices
    }

    @CompileStatic
    EnterpriseService findServiceByUriString(String uriString) {
        EnterpriseService retService = null
        enterpriseServices.each { EnterpriseService service ->
            if (service.uriString == uriString) retService = service  // there will be only one
        }

        retService
    }

    @CompileStatic void startCronScheduler() {
        if (cronServices.size() > 0) {
            cronScheduler = new Scheduler()
            cronServices.each { CronService service ->
                cronScheduler.schedule(service.cronString, {
                    service.handlers['default'].execute([] as Object[])
                } as Runnable )
            }
            cronScheduler.start()
        }

    }

    @CompileStatic void stopCronScheduler() {
        if (cronScheduler) {
            cronScheduler.stop()
            cronScheduler = null
        }
    }

    @CompileStatic void resetCronScheduler() {
        stopCronScheduler()
        startCronScheduler()
    }

}
