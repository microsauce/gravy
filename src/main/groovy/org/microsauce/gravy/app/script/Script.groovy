package org.microsauce.gravy.app.script

class Script {
	String name
	String sourceUri
	List<String> roots = []
	List<String> classPathUris = []
	Map binding = [:]

	String getSource() {
		new File(sourceUri).name
	}
}