package org.microsauce.gravy.lang.groovy

import org.microsauce.gravy.lang.object.Serializer
import org.ho.yaml.Yaml

/**
 * Created with IntelliJ IDEA.
 * User: jboone
 * Date: 2/15/13
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
class GroovyYamlSerializer implements Serializer {
    @Override
    Object parse(String string) {
        return Yaml.load(string)
    }

    @Override
    String toString(Object object) {
        return Yaml.dump(object)
    }
}
