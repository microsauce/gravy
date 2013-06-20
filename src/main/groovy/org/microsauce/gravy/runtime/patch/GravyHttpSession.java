package org.microsauce.gravy.runtime.patch;

import javax.servlet.http.HttpSession;

public interface GravyHttpSession extends HttpSession {
    public Object get(String key);

    public void put(String key, Object value);

    public void redirect(String url);
}
