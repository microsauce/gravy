package org.microsauce.gravy.lang.object

import groovy.transform.CompileStatic

enum GravyType {
    GROOVY('Groovy'), JAVASCRIPT('JavaScript'), RUBY('Ruby')

    String name;

    GravyType(String name) {
        this.name = name;
    }

//    @CompileStatic boolean equals(o) {
//        if (this.is(o)) return true
//        if (getClass() != o.class) return false
//        if (!super.equals(o)) return false
//
//        GravyType gravyType = (GravyType) o
//
//        if (name != gravyType.name) return false
//
//        return true
//    }

//    @CompileStatic int hashCode() {
//        int result = super.hashCode()
//        result = 31 * result + (name != null ? name.hashCode() : 0)
//        return result
//    }
}
