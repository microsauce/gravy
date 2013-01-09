/*******************************************************

This script defines the core JavaScript API

Script bindings:

'out'			- the console PrintStream

*******************************************************/

global.services = new Object();

/********************************************************
 * documented utility/convenience functions
 *******************************************************/

/*
 * JSON -> obj
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


global.parseJson = function(jsonText) {
	return JSON.parse(jsonText, reviver)
}

global.getGlobal = function() {
	return (function(){
		return this;
	}).call(null);
}

/*
 * retrieve a configuration value 
 */
global.conf = function(key) {
	return config.getProperty(key)
}

/*
 * print a line to the console
 */

global.println = function(str) {
	return out.println(str)
}

/*
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
