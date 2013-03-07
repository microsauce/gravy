package org.microsauce.gravy.runtime.resolver

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j

import java.util.concurrent.ConcurrentHashMap

@Log4j
class ResourceResolver {

	Map<String,RealPath> resolvedPaths = new ConcurrentHashMap()	// uri - real path
	Map<String,String> resolvedUris = new ConcurrentHashMap()		// uri - real path
	Map<String,byte[]> cachedFiles = new ConcurrentHashMap() 		// real path - file content // TODO 
	List<String> roots = []
	List<CacheConstraints> constraints = []
	String basedir

	ResourceResolver(String basedir) {
		if ( !basedir ) throw new RuntimeException("basedir may not be null")
		else this.basedir = basedir
	}

	@CompileStatic
	private String resolve(String uri) throws FileNotFoundException {
		RealPath realPath = resolveUri(uri)
		realPath.path
	}

	@CompileStatic
	private RealPath resolveUri(String uri) throws FileNotFoundException {
        log.debug "resolve $uri to real path"
		RealPath realPath = resolvedPaths[uri]
        if ( !realPath ) {
			log.debug "real path not cached - resolve from resource path"

			for ( String thisRoot in this.roots ) {
				File file = new File(thisRoot+uri)
				log.debug "checking source path ${thisRoot+uri}"
				if ( file.exists() ) {
					realPath = new RealPath()
					realPath.path = file.absolutePath
					resolvedPaths[uri] = realPath
					log.debug "uri $uri resolved to real path ${realPath.path}"
					break
				}
			}
			if ( !realPath ) 
				throw new FileNotFoundException("Cannot resolve uri $uri to real path. Path roots: $roots")
		}

		realPath
	}

	@CompileStatic
	String realUri(String uri) throws FileNotFoundException {
		String realUri = resolvedUris[uri]
		log.debug "resolve $uri to real uri"
		if ( !realUri )  {
			RealPath realPath = resolveUri uri

			realUri = realPath.path - basedir
			resolvedUris[uri] = realUri
			log.debug "$uri resolved to real uri $realUri"
		}
		realUri
	}

	@CompileStatic
	byte[] retrieve(String uri) throws FileNotFoundException { // TODO return an output stream
		byte[] bytes = null
		RealPath realPath = resolveUri(uri)
		if ( realPath.cached == null )
			realPath.cached = cacheFile realPath.path
		if ( realPath.cached ) {
			bytes = cachedFiles[realPath]
			if ( !bytes ) {
				bytes = new File(realPath.path).readBytes()
				cachedFiles[realPath.path] = bytes
			}
		} else bytes = new File(realPath.path).readBytes()

		bytes // TODO byte array output stream ???
	}

	@CompileStatic
	private Boolean cacheFile(String realPath) {
		boolean cacheFile = false
		for ( CacheConstraints constraint in constraints ) {
			if ( constraint.cacheFile(realPath) ) return true
		}

		false
	}

	private class RealPath {
		String path
		Boolean cached = null
	}

}