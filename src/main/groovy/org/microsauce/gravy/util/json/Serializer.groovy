package org.microsauce.gravy.util.json;

public interface Serializer {

	String stringify(Object object)
	
	Object parse(String string)
	
}
