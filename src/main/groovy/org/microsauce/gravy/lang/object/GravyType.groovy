package org.microsauce.gravy.lang.object

enum GravyType {
    GROOVY("Groovy"), JAVASCRIPT("JavaScript"), RUBY('Ruby')

    String type;

    GravyType(String type) {
        this.type = type;
    }

}
