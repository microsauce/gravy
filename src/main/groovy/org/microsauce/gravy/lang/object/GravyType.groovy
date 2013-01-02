package org.microsauce.gravy.lang.object

enum GravyType {
	GROOVY("Groovy"), JAVASCRIPT("JavaScript")
	
	String type;
	
	GravyType(String type) {
		this.type = type;
	}
	
}
