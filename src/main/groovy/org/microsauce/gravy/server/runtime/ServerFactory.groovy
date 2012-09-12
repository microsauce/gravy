package org.microsauce.gravy.server.runtime

import groovy.util.logging.Log4j

@Log4j
class ServerFactory {

	def static private wrappers = [
//		tomcat : TomcatWrapper,
		jetty : JettyWrapper2
//		resin : ResinWrapper
	]

	static ServerWrapper getServer(config) {
		return wrappers[ config.server ?: 'jetty' ].newInstance(config)
	}
}