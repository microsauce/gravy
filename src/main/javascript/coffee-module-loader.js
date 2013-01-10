/**
 * This script defines a RingoJS extension for loading modules written 
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
		var jsSource = coffeec.CoffeeScript.compile(coffeeSource);
		fs.write(cc.getPath(), jsSource);
		jsContent = jsSource;
	} else jsContent = cc.getContent();
	
	return jsContent;
};
console.log("\tComplete.");

