package org.microsauce.gravy.runtime

import javax.servlet.Filter
import javax.servlet.DispatcherType

class FilterWrapper {
    Filter filter
    String mapping
    EnumSet<DispatcherType> dispatch
}