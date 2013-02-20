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
	return JSON.load(jsonText, reviver)
}

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
