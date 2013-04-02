
/*
    define ringo extensions
*/

var wrapMod = function(scriptText) {
    return '(function(gravy) { this.gravyModule = gravy.gravyModule; var get = gravy.get; var post = gravy.post; var put = gravy.put; var del = gravy.del;  var use = gravy.use;  var route = gravy.route; var conf = gravy.conf; var log = gravy.log; ' + scriptText + '\n})(new GravyModule(j_module,j_config,j_logger))';
}

var isAppScript = function(path) {
    var modScriptPattern = /.*\/WEB-INF\/modules\/[a-zA-Z0-9\-_]+\/application.*/
    return modScriptPattern.test(path);
}

/**
 * This section defines a RingoJS extension for loading modules written
 * in CoffeeScript.
 */

console.log("adding CoffeeScript extension . . .");
if ( devMode == 'true' ) {
	var engine = require('ringo/engine')
	engine.setOptimizationLevel(-1) // necessary for coffee-script.js
	var fs = require('fs');
	var coffeec = require('coffee-script');
}

require.extensions[".coffee"] = function(coffee) {
	var jsContent = null;
	var cc = new org.ringojs.repository.FileResource(coffee.getPath()+".js")
	if (!cc.exists() || coffee.lastModified() > cc.lastModified()) {
		var coffeeSource = coffee.getContent();
		var jsSource
		if (isAppScript(coffee.getPath()))
		    jsSource = coffeec.CoffeeScript.compile(coffeeSource, {bare: true});
		else
		    jsSource = coffeec.CoffeeScript.compile(coffeeSource);

		fs.write(cc.getPath(), jsSource);
		jsContent = jsSource;
	} else jsContent = cc.getContent();

    if ( isAppScript(coffee.getPath()) )
        jsContent = wrapMod( jsContent )
	return jsContent;
};
console.log("\tComplete.");

/**
 * This section defines a RingoJS extension for loading modules written
 * in CoffeeScript.
 */

console.log("adding gravy extension . . .");
require.extensions[".js"] = function(script) {
	var jsContent = null;
    if ( isAppScript(script.getPath()) )
        jsContent = wrapMod(script.getContent())
	else jsContent = script.getContent();


	return jsContent;
};
console.log("\tComplete.");