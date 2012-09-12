package org.microsauce.gravy.app

import groovy.util.logging.Log4j
import org.microsauce.gravy.app.script.*
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.*
import static groovy.io.FileType.DIRECTORIES
import static java.nio.file.StandardWatchEventKinds.*

/**
* Currently we only observe application.groovy
* TODO utilize nio or Jnotify
* TODO add registerSource - observe more than just the root script
*	register module and script folders
*	compiled source folders
*/

@Log4j
class NioSourceModObserver implements SourceModObserver {

	private static int pollInterval = 500
	def private static scriptSources

	def config
	def scriptPaths 
	def compileSourcePaths

	def scriptPatterns = [
		/application\.groovy/,
		/scripts.*\.groovy/,
		/modules.*\.groovy/
	]
	def compiledSourcePattern = /src.main.(java|groovy|resources)/

	NioSourceModObserver(config) {
		this.config = config
	}

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

	def compiledSourceHandlers = []
	def scriptHandlers = []

	void addScriptHandler(SourceModHandler handler) {
		scriptHandlers << handler
	}

	void addCompiledSourceHandler(SourceModHandler handler) {
		compiledSourceHandlers << handler
	}

	void start() {
		WatchService watcher = 
            FileSystems.getDefault().newWatchService()
        registerTree(config.appRoot, watcher)

		// start watcher thread
		Thread.start {
			while (true) {
				handleEvents(watcher, scriptHandlers, compiledSourceHandlers)
			}
		}

	}

	def private registerPath(file, watcher) {
		log.info "observing path ${file.absolutePath}"
		Path path = Paths.get(file.absolutePath)
		path.register(
            watcher,
            StandardWatchEventKinds.ENTRY_MODIFY)
	}

	def private registerTree(pathName, watcher) {
		File file = new File(pathName)
		if (file.exists() && file.isDirectory()) {
			file.eachFileRecurse(DIRECTORIES) { subFolder ->
				registerPath(subFolder, watcher)
			}						
		}
	}

	def private handleEvents(watcher, scriptHandlers, compiledSourceHandlers) {
		WatchKey key = watcher.take()
		Path path = (Path)key.watchable();

        for ( WatchEvent<?> event: key.pollEvents()){
println "&&&&&&&&&&&&&&&&&& event ${event}"        	
			Path fullPath = path.resolve(event.context())
println "&&&&&&&&&&&&&&&&&& fullPath ${fullPath}"        	

            WatchEvent.Kind kind = event.kind()
            if (kind == OVERFLOW) continue

println "@@@@@@@@@@@@@@@@ ${kind.name()} - ${event.context()}"           
            switch (kind.name()){
                case "ENTRY_MODIFY":
                    if (isScript(fullPath.toString())) {
                    	log.info "Modified script: $fullPath"
	                    scriptHandlers.each { thisHandler ->
	                    	thisHandler.handle()
	                    }
	                } else if (isCompiledSource(fullPath.toString())) {
                    	log.info "Modified source: $fullPath"
	                    compiledSourceHandlers.each { thisHandler ->
	                    	thisHandler.handle()
	                    }
	                }

            }
        }

        key.reset()
	}

	private boolean isCompiledSource(String path) {
		path ==~ compiledSourcePattern
	}

	private boolean isScript(String path) {
		for (thisPattern in scriptPatterns) {
			if (path ==~ thisPattern) return true
		}

		false
	}

}
