package org.microsauce.gravy.lang.patch

import groovy.transform.CompileStatic

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * Use this class to monkey patch the Java Enterprise runtime
 * 
 * @author jboone
 *
 */
abstract class BaseEnterpriseProxy implements InvocationHandler {
	
	protected Object target
	
	BaseEnterpriseProxy(Object target) {
		this.target = target
	}
	
	@CompileStatic
	public Object invoke(final Object proxy, final Method method,
		final Object[] args) throws Throwable {
		Object value = null
		try {
			Method targetMethod = target.class.getMethod(method.name, method.parameterTypes)
			value = targetMethod.invoke(target, args)
		}
		catch(NoSuchMethodException nsme) { 
			Method targetMethod = this.class.getMethod(method.name, method.parameterTypes)
			value = targetMethod.invoke(this, args)
		}

		return value
	}

}
