package org.microsauce.gravy.util.script.groovy

class Script {
	String name
	String sourceUri
	List<String> roots = []
	List<String> classPathUris = []
	Map binding = [:]
	ClassLoader classLoader

	String getSource() { // TODO review this
		new File(sourceUri).name
	}
}