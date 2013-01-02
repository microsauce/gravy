package org.microsauce.gravy.lang.object

interface Serializer {
	Object parse(String string);
	String toString(Object object);
}
