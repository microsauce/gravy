package org.microsauce.gravy.lang

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.apache.log4j.Logger
import org.ringojs.engine.RhinoEngine
import org.ringojs.engine.RingoConfig
import org.ringojs.repository.FileRepository
import org.ringojs.repository.FileResource
import org.ringojs.repository.Repository
import org.ringojs.repository.ZipRepository

/**
 * Created with IntelliJ IDEA.
 * User: microsauce
 * Date: 4/1/13
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */
abstract class Runtime {

    List<File> roots
    Logger logger

    @CompileStatic Runtime() {
        logger = Logger.getLogger(this.class)
    }

    abstract Object run(String scriptUri, Map<String, Object> binding)

    abstract Object run(File script, Map<String, Object> binding)

    @CompileStatic void appendRoots(List<File> roots) {
        this.roots.addAll(roots)
    }

    @CompileStatic void prependRoots(List<File> roots) {
        this.roots.addAll(0,roots)
    }

}
