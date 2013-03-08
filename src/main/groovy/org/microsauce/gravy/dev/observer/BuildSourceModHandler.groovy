package org.microsauce.gravy.dev.observer

import org.microsauce.gravy.context.Context
import org.microsauce.gravy.dev.lifecycle.Lifecycle


class BuildSourceModHandler implements SourceModHandler {

    BuildSourceModHandler() {}

    void handle() {
        try {
            def builder = new Lifecycle()
            builder.compile()
        }
        catch (all) {
            all.printStackTrace()
        }
    }
}