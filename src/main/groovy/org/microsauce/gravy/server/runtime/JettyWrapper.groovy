package org.microsauce.gravy.server.runtime

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.server.Handler
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.microsauce.gravy.dev.DevUtils
import groovy.util.logging.Log4j

@Log4j
class JettyWrapper extends ServerWrapper {

	def private server

	JettyWrapper(config) {
		super(config)
	}

	void initialize() {
		log.info 'initializing embedded jetty'
		server = new Server(config.jetty.port)

        WebAppContext context = new WebAppContext()
println "descriptor: ${getDescriptorURI(config.appRoot)}"        
        context.setDescriptor(getDescriptorURI(config.appRoot))
println "resourceBase: ${getResourceBase(config.appRoot)}"        
        context.setResourceBase(getResourceBase(config.appRoot)) 
        context.setContextPath(config.jetty.contextPath)
        context.setParentLoaderPriority(true)
 
        server.setHandler(context);
	}

	void start() {
		server.start()
		server.join()
	}

	void stop() {
		server.stop()
	}

	private String getDescriptorURI(appRoot) { 
		getResourceBase(appRoot)+'/WEB-INF/web.xml'
	}

	private String getResourceBase(String appRoot) {
		String appName = new File(appRoot).name
		DevUtils.appDeployPath(appName)
	}


}