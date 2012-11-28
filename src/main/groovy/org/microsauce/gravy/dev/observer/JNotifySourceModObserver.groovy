package org.microsauce.gravy.dev.observer

import net.contentobjects.jnotify.*
import java.lang.reflect.*
import groovy.util.logging.Log4j

@Log4j
class JNotifySourceModObserver implements SourceModObserver {

	private static final int IGNORE_THREASHOLD = 1000

	private List<SourceModHandler> scriptHandlers = []
	private List<SourceModHandler> sourceHandlers = []

	private ConfigObject config

	JNotifySourceModObserver(ConfigObject config) {
		this.config = config
		addLibraryPath()
	}

	public void addScriptHandler(SourceModHandler handler) {
		scriptHandlers << handler
	}

	public void addCompiledSourceHandler(SourceModHandler handler) {
		sourceHandlers << handler
	}

	public void start() {
		int mask = JNotify.FILE_MODIFIED

		ScriptListener scriptListenter = new ScriptListener()
		SourceListener sourceListener = new SourceListener()

    	if (exists("${config.gravy.project}")) 
    		JNotify.addWatch("${config.gravy.project}", mask, false, scriptListenter)
    	if (exists("${config.gravy.project}/scripts")) 
    		JNotify.addWatch("${config.gravy.project}/scripts", mask, false, scriptListenter)
    	if (exists("${config.gravy.project}/src/main")) 
    		JNotify.addWatch("${config.gravy.project}/src/main", mask, true, sourceListener)

	}

	private boolean exists(String path) {
		new File(path).exists()
	}

	private void addLibraryPath() throws IOException {
	    try {
			// This enables the java.library.path to be modified at runtime
			// From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
			//
			String s = "${System.getenv()['GRAVY_HOME']}/lib/jnotify".toString();
			log.info "adding $s to java.library.path"
			Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			String[] paths = (String[])field.get(null);
			for (int i = 0; i < paths.length; i++) {
				if (s.equals(paths[i])) {
				  return;
				}
			}
			String[] tmp = new String[paths.length+1];
			System.arraycopy(paths,0,tmp,0,paths.length);
			tmp[paths.length] = s;
			field.set(null,tmp);
			System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + s);
			log.info "java.library.path = ${System.getProperty("java.library.path")}"
	    } catch (IllegalAccessException e) {
			throw new IOException("Failed to get permissions to set library path");
	    } catch (NoSuchFieldException e) {
			throw new IOException("Failed to get field handle to set library path");
	    }
	}

	private static Map lastEvent = [:]

	/*
		this is a work around for an apparent bug in JNotify, which fires multiple
		fileModified events each time an event occurs		
	*/
	private boolean ignoreEvent(String path) {
		synchronized(lastEvent) {

			boolean ignoreEvent = true
			Long currentTime = System.currentTimeMillis()
			Long timeStamp = lastEvent[path]
			if (timeStamp) {
				if ((currentTime-timeStamp) > IGNORE_THREASHOLD) {
					lastEvent[path] = currentTime
					ignoreEvent = false
				}
			} else {
				ignoreEvent = false
				lastEvent[path] = currentTime
			}

			return ignoreEvent
		}	
	}

	private void fileModified(String rootPath, String name, List<SourceModHandler> handlers) {
		String absolutePath = rootPath+File.separator+name
		if (ignoreEvent(absolutePath)) return

		// handle event
		handlers.each { thisHandler ->
			thisHandler.handle()
		}
	}

	private class ScriptListener implements JNotifyListener {

		public void fileModified(int wd, String rootPath, String name) {
			Thread.start {
				fileModified(rootPath, name, scriptHandlers)
			}
		}
		public void fileRenamed(int wd, String rootPath, String oldName, String newName) {}
		public void fileDeleted(int wd, String rootPath, String name) {}
		public void fileCreated(int wd, String rootPath, String name) {}
	}

	private class SourceListener implements JNotifyListener {
		public void fileModified(int wd, String rootPath, String name) {
			Thread.start {
				fileModified(rootPath, name, sourceHandlers)
			}
		}
		public void fileRenamed(int wd, String rootPath, String oldName, String newName) {}
		public void fileDeleted(int wd, String rootPath, String name) {}
		public void fileCreated(int wd, String rootPath, String name) {}
	}
}