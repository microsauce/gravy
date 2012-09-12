
// TODO exclude Jetty jars


def basedir = System.getProperty('user.dir')
def sysEnv = System.getenv()
def gravyHome = sysEnv['GRAVY_HOME']
if (!gravyHome) {
	println 'You must set your GRAVY_HOME environment variable.'
	System.exit(1)
}
def tokenizedPath = basedir.split(String.valueOf(File.separatorChar))
def appName = tokenizedPath[tokenizedPath.length-1]


def ant = new AntBuilder()

// compile project sources
ant.sequential {

	mkdir(dir:"${basedir}/target/classes")

	//
	// define classpath
	//
	echo 'build classpath'
	path(id:"build.classpath") {
		fileset(dir:"${gravyHome}/lib") {
			include(name:'**/*.jar')
		}
		fileset(dir:"${basedir}/lib") {
			include(name:'**/*.jar')
		}
		pathelement(path:"${basedir}/target/classes")
	}

	// compile java
	if (exists("${basedir}/src/main/java")) {
		echo 'compile java'
		javac(
				destdir:"${basedir}/target/classes",
				classpathref:"build.classpath",
				deprecation:"off") {
			src(path:"${basedir}/src/main/java")
		}
	} else {
		echo 'no java sources found'
	}

	// compile groovy
	if (exists("${basedir}/src/main/groovy")) {
		echo 'compile groovy'
		taskdef(
			name:'groovyc', 
			classname:'org.codehaus.groovy.ant.Groovyc', 
			classpathref:'build.classpath')
		groovyc(
			destdir:"${basedir}/target/classes",
			srcdir:"${basedir}/src/main/groovy",
			listfiles:'true') {
			classpath(refid:'build.classpath')
		}
	} else {
		echo 'no groovy sources found'
	}

	// copy resources
	if (exists("${basedir}/src/main/resources")) {
	    copy(todir:"${basedir}/target/classes") {
	    	fileset(dir:"${basedir}/src/main/resources") {
	    		include(name:'*/**')
	    	}
	    }
    }

}

// assemble war
ant.sequential {

	def tempWar = "${basedir}/target/${appName}"
	if (exists("${tempWar}")) delete(dir:"${tempWar}")
	if (exists("${tempWar}.war")) delete(dir:"${tempWar}.war")

	mkdir(dir:"${tempWar}")
    copy(todir:"${tempWar}") {
    	fileset(dir:"${basedir}/webroot") {
    		include(name:'*/**')
    	}
    }
    copy(todir:"${tempWar}/WEB-INF") {
    	fileset(dir:"${basedir}") {
    		include(name:'scripts/**')
    		include(name:'view/**')
    		include(name:'conf/**')
    		include(name:'modules/**')
    		include(name:'application.groovy')

    	}
    }
    copy(file:"${basedir}/application.groovy", todir:"${tempWar}/WEB-INF")

    // class files and resources
    mkdir(dir:"${tempWar}/WEB-INF/lib")
    mkdir(dir:"${tempWar}/WEB-INF/classes")
    if (new File("${basedir}/target/classes").exists()) {
	    copy(todir:"${tempWar}/WEB-INF/classes") {
	    	fileset(dir:"${basedir}/target/classes") {
	    		include(name:'*/**')
	    	}
	    }
    }
    // libraries
    if (new File("${basedir}/lib").exists()) {
	    copy(todir:"${tempWar}/WEB-INF/lib") {
	    	fileset(dir:"${basedir}/lib") {
	    		include(name:'*/**')
	    	}
	    }
    }
	copy(todir:"${tempWar}/WEB-INF/lib", flatten:'true') {
    	fileset(dir:"${gravyHome}/lib") {
    		include(name:'*/**')
    	}
    }

	zip(destfile:"${tempWar}.war", basedir:"${tempWar}")
	delete(dir:"${tempWar}")
}

def exists(path) {
	new File(path).exists()
}