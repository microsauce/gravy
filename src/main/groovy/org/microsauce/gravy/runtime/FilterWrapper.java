package org.microsauce.gravy.runtime;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.EnumSet;

public class FilterWrapper {
    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public String getMapping() {
        return mapping;
    }

    public void setMapping(String mapping) {
        this.mapping = mapping;
    }

    public EnumSet<DispatcherType> getDispatch() {
        return dispatch;
    }

    public void setDispatch(EnumSet<DispatcherType> dispatch) {
        this.dispatch = dispatch;
    }

    private Filter filter;
    private String mapping;
    private EnumSet<DispatcherType> dispatch;
}
