package org.microsauce.gravy.runtime.patch;

import javax.servlet.http.HttpServletRequest;

public interface GravyHttpServletRequest extends HttpServletRequest {
    public Object get(String key);

    public Object getIn();

    public void setIn(Object _in);

    public void put(String key, Object value);

    public void next();

    public void forward(String uri);

    public GravyHttpSession session();
}
