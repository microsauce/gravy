package org.microsauce.gravy.util.json

import org.mozilla.javascript.NativeJSON
import org.mozilla.javascript.NativeObject

class JSSerializer implements Serializer {

	
	
	@Override
	public String stringify(Object object) {
		NativeJSON.stringify(ctx, scope, object, null, null)
	}

	@Override
	public Object parse(String json) {
		null
	}

}
