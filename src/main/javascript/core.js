/**
 * This script defines core functions.
 *
 * Script bindings:
 * 'out'			- the console PrintStream
 */

/**
 * The module services object.
 */
global.services = new Object();

/********************************************************
 * documented utility/convenience functions
 *******************************************************/

/**
 * JavaScript JSON serialization functions
 */
global.datePatternJS = new RegExp('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}Z')
global.datePatternJV = new RegExp('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\+[0-9]{4}')
global.reviver = function (key, value) {
    if (typeof(value)=='string') {
        if ( datePatternJS.test(value) ) {
            return new Date(Date.parse(value))
        }
        else if ( datePatternJV.test(value) ) {
            var jsValue = value.replace(/\\+[0-9]{4}/g, '.000Z')
            return new Date(Date.parse(jsValue))
        }
    }
    
    return value
}

/*global.replacer = function(key, value) {
	if (Object.prototype.toString.call(value)==='[object Date]') { 
		// toISOString minus 'millis' and Z - these are non-standard
		value = value.toISOString().substring(0, 19)
	}
	return value
}
*/
global.parseJson = function(jsonText) {
	return JSON.parse(jsonText, reviver)
}

/*global.stringify = function(object) {
	return JSON.stringify(object, replacer)
}
*/
global.getGlobal = function() {
	return (function(){
		return this;
	}).call(null);
}

/**
 * Retrieve a configuration value (from config.groovy) 
 */
global.conf = function(key) {
	return config.getProperty(key)
}

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
