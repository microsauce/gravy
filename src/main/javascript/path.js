
var strings = require('ringo/utils/strings')

export('join', 'extname', 'dirname')

var join = function() {
console.log("in join")	
	var buffer = ''
	Array.forEach(arguments, function(arg) {
		buffer += arg
	});
	
	return buffer
}

var extname = function(p) {
	var ext = '';
	var lstNdx = p.lastIndexOf('.');
	if ( lstNdx != -1 ) {
		ext = p.substring(lstNdx, p.length)
	}
	return ext;
}

var dirname = function(p) {
	var ext = '';
	var lstNdx = p.lastIndexOf('/');
	if ( lstNdx != -1 ) {
		ext = p.substring(0, lstNdx)
	}
	return ext;
}