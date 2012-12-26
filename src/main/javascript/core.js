/*******************************************************

This script defines the core JavaScript API

Script bindings:

'log'			- the application logger
'out'			- the console PrintStream
'util'			- a utility class - IO functions - etc

*******************************************************/

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

