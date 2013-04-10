/**
 * This script defines core functions.
 *
 * Script bindings:
 * 'out'			- the console PrintStream
 */

/**
 * The module services object.
 */
global.exp = new Object();

/********************************************************
 * documented utility/convenience functions
 *******************************************************/

global.initModuleConfig = function (config) {
    var jsObject = {}
    var iterator = config.keySet().iterator()
    while ( iterator.hasNext() ) {
        var thisKey = iterator.next()
        var thisValue = config.get(thisKey)
        if ( thisValue instanceof Packages.groovy.util.ConfigObject ) {
            jsObject[thisKey] = initModuleConfig(thisValue)
        }
        else {
            jsObject[thisKey] = thisValue
        }
    }

    return jsObject
}

//global.conf = global.initModuleConfig(config)

/**
 * JavaScript JSON serialization functions
 */
global.datePattern  = new RegExp('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}')
global.reviver = function (key, value) {
    if (typeof(value)=='string' && value.length >= 19) {
    	var subString = value.substring(0, 19)
    	
        if ( datePattern.test(subString) ) {
            var jsValue = subString+'.000Z'
            return new Date(Date.parse(jsValue))
        }
    }
    
    return value
}

global.parseJson = function(jsonText) {
console.log("parseJson: " + jsonText)
	return JSON.parse(jsonText, reviver)
}

global.getGlobal = function() {
	return (function(){
		return this;
	}).call(null);
}

/**
 * Retrieve a configuration value (from config.groovy) 
 */
//global.conf = function(key) {
//	return config.getProperty(key)
//}

/**
 * print a line to the console
 */
global.println = function(str) {
	return out.println(str)
}

/**
 * print a string to the console
 */
global.print = function(str) {
	return out.print(str)
}

/*******************************************************
 * undocumented utility functions/classes
 ******************************************************/

String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1
};
