package org.microsauce.gravy.runtime

import groovy.transform.CompileStatic

class Error {

    String errorCode
    String message
    String stackTrace
    Throwable throwable

    Error(Throwable t) {
        init(t)
    }

    @CompileStatic
    private void init(Throwable t) {
        this.errorCode = errorCode()
        this.throwable = t
        this.message = throwable.message
        StringWriter stringWriter = new StringWriter()
        PrintWriter printWriter = new PrintWriter(stringWriter)
        t.printStackTrace printWriter
        stackTrace = stringWriter.toString()
    }

    @CompileStatic
    private String errorCode() {
        def addr = InetAddress.getLocalHost();
        byte[] ipAddr = addr.getAddress();
        def hostName = addr.getHostName()
        "${hostName}-${System.currentTimeMillis()}".toString()
    }

}
