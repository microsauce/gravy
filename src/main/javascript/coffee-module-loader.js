
console.log("adding CoffeeScript extension . . .");
if ( devMode == 'true' ) { 
	var engine = require('ringo/engine')
	engine.setOptimizationLevel(-1)
	var fs = require('fs');
	var coffeec = require('coffee-script'); 
}

require.extensions[".coffee"] = function(coffee) {
console.log('load module: '+coffee.getPath())	
	var jsContent = null;
	var cc = new org.ringojs.repository.FileResource(coffee.getPath()+".js")
console.log('1')	
	if (!cc.exists() || coffee.lastModified() > cc.lastModified()) {
console.log('2')	
		var coffeeSource = coffee.getContent();
		var jsSource = coffeec.CoffeeScript.compile(coffeeSource);
		fs.write(cc.getPath(), jsSource);
		jsContent = jsSource;
console.log('3')	
	} else {
console.log('4')	
		jsContent = cc.getContent();
	}
console.log('5: '+jsContent)	
	
	return jsContent;
};
console.log("\tComplete.");

