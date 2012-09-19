package org.microsauce.gravy

import groovy.util.logging.Log4j
import java.net.URLClassLoader
import groovy.transform.CompileStatic

@Log4j
class ModuleClassLoader extends URLClassLoader {

	public ModuleClassLoader(ClassLoader parent) {
        super(new URL[0], parent)
    }

	@CompileStatic
    void addPath(String path) throws MalformedURLException, ClassNotFoundException {
        File file = new File(path)
        if (file.exists()) {
        	log.info "adding classpath element $path"
	        URI uri = file.toURI()
	        URL url = uri.toURL()

	        addURL(url)
	    }
    }
}