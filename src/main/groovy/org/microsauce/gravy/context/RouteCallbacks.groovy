package org.microsauce.gravy.context

import groovy.transform.CompileStatic
import org.microsauce.gravy.lang.object.GravyType

/**
 * Created with IntelliJ IDEA.
 * User: jboone
 * Date: 4/15/13
 * Time: 1:02 PM
 * To change this template use File | Settings | File Templates.
 */
class RouteCallbacks extends ArrayList<Handler> {
    boolean polyglot = false
    GravyType firstType

    @CompileStatic boolean add(Handler handler) {
        if ( !firstType ) firstType = handler.module.type
        else if (!polyglot)
            polyglot = !handler.module.type.name.equals(firstType.name) // TODO
        return super.add(handler)
    }
}
