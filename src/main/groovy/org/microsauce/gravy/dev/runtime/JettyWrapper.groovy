package org.microsauce.gravy.dev.runtime

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
        String deployPath = DevUtils.appDeployPath(System.getProperty('user.dir'))
        context.setDescriptor(getDescriptorURI(deployPath))
        context.setResourceBase(deployPath)
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
        appRoot + '/WEB-INF/web.xml'
    }

}