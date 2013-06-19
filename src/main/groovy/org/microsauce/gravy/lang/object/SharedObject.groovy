package org.microsauce.gravy.lang.object

interface SharedObject {
	
	Object value(GravyType destType)
	
	Object toNative()
	
	String toString()
		
}
