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
			try {
				Method targetMethod = target.class.getDeclaredMethod(method.name, method.parameterTypes)
				value = targetMethod.invoke(target, args)
			}
			catch(NoSuchMethodException nsme) { // check the superclass (TODO make sure this is necessary)
				Method targetMethod = target.class.superclass.getDeclaredMethod(method.name, method.parameterTypes)
				value = targetMethod.invoke(target, args)
			}
		}
		catch (NoSuchMethodException nsme) {
			// check the handler class for method signature
			Method targetMethod = this.class.getDeclaredMethod(method.name, method.parameterTypes)
			value = targetMethod.invoke(this, args)
		}

		return value
	}

}
