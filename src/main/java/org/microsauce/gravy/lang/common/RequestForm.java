package org.microsauce.gravy.lang.common;

import org.microsauce.gravy.context.ServletFacade;

import java.io.InputStream;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: microsauce
 * Date: 4/8/13
 * Time: 9:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class RequestForm extends HashMap {
    private HashMap<Object,InputStream> fileInputs;
    private ServletFacade facade;

    public RequestForm(ServletFacade facade) {
        fileInputs = new HashMap<Object,InputStream>();
        this.facade = facade;
    }

    public Object put(Object key, Object value) {
        if ( value instanceof InputStream )
            fileInputs.put(key, (InputStream)value);

        return super.put(key, value);
    }

    public Object get(Object key) {
        InputStream is = fileInputs.get(key);
        if ( is != null )
            return facade.getContextualizedInput(is);
        else return super.get(key);
    }
}
