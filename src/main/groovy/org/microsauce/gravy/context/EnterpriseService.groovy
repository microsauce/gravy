package org.microsauce.gravy.context

import java.util.regex.Pattern

class EnterpriseService extends Service {

    public static String GET = 'get'
    public static String POST = 'post'
    public static String PUT = 'put'
    public static String OPTIONS = 'options'
    public static String DELETE = 'delete'
//    public static String DEFAULT = 'default'
    public static String MIDDLEWARE = 'default'

    List<Handler> middleware = []

    Pattern uriPattern
    String uriString
    boolean endPoint
}
