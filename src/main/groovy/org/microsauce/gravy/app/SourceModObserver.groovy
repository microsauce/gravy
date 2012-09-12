package org.microsauce.gravy.app


/**
* Currently we only observe application.groovy
* TODO utilize nio or Jnotify
* TODO add registerSource - observe more than just the root script
*	register module and script folders
*	compiled source folders
*/

interface SourceModObserver {

	public void  addScriptHandler(SourceModHandler handler) 

	public void addCompiledSourceHandler(SourceModHandler handler)

	public void start()

/*
	def start() {
		Script application = new Script([sourceUri: './application.groovy'])

		Thread.start {
			println 'Begin polling application sources for changes . . .'
			def dateModified = new File('./application.groovy').lastModified()
			while (true) {
				Thread.sleep(pollInterval) 
				def currentLastModified = new File('./application.groovy').lastModified()
				if (currentLastModified > dateModified) {
					dateModified = currentLastModified
					handlers.each { handler ->
						handler.handle()
					}
				}
			}
		}

	}
*/		


}
