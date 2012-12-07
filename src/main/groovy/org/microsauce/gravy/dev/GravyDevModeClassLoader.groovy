package org.microsauce.gravy.dev;

import groovy.transform.CompileStatic

/*
    When running your application in devMode (via the gravy command) we pass 
    an instance of this classloader to the GroovyScriptEngine.  
*/
public class GravyDevModeClassLoader extends ClassLoader {
	
    private Map classes = new HashMap<String, Class>()
    private String folder

    public GravyDevModeClassLoader(String folder) {
        super(GravyDevModeClassLoader.class.getClassLoader())
        this.folder = folder
    }

    @CompileStatic
    public Class findClass(String className) {
        String path = className.replaceAll("\\.", '/')+".class"
        File classFile = new File(folder+'/'+path)
        if (classFile.exists()) {
            try {
                Class result

                def bytes = classFile.bytes
                result = defineClass(className, bytes, 0, bytes.length, null)

                classes.put(className, result)
                return result
            } catch (Exception e) {
                e.printStackTrace()
                return null
            }
        } else return null

    } 
}