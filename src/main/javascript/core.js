/*******************************************************

This script defines the core JavaScript API

Script bindings:

'log'			- the application logger
'out'			- the console PrintStream
'util'			- a utility class - IO functions - etc

*******************************************************/

/********************************************************
 * documented utility/convenience functions
 *******************************************************/

/*
 * JSON -> obj
 */
var datePatternJS = new RegExp('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}Z')
var datePatternJV = new RegExp('[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\+[0-9]{4}')
function reviver(key, value) {
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


function parseJson(jsonText) {
	return JSON.parse(jsonText, reviver)
}

function getGlobal() {
	return (function(){
		return this;
	}).call(null);
}

/*
 * script loader - loads JavaScripts and CoffeeScripts
 */
var load = function(scriptUri) {
	return util.load(scriptUri)
}

/*
 * retrieve a configuration value 
 */
var conf = function(key) {
	return config.getProperty(key)
}

var readFile = function(filePath) {
	util.readFileAsString(filePath)
}

/*
 * print a line to the console
 */
function getGlobal(){
	return (function(){
		return this;
	}).call(null);
}
 
var println = function(str) {
	return out.println(str)
}

/*
 * print a string to the console
 */
var print = function(str) {
	return out.print(str)
}

/*******************************************************
 * undocumented utility functions/classes
 ******************************************************/

String.prototype.endsWith = function(suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1
};

var require = function(uri) {
	return util.require(scriptUri)
}
