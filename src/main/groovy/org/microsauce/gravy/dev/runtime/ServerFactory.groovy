package org.microsauce.gravy.dev.runtime

import groovy.util.logging.Log4j

@Log4j
class ServerFactory {

	def static private wrappers = [
		jetty : JettyWrapper
//		tomcat : TomcatWrapper,
//		resin : ResinWrapper
	]

	static ServerWrapper getServer(config) {
		return wrappers[ config.server ?: 'jetty' ].newInstance(config)
	}
}