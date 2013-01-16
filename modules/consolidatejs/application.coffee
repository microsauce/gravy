
###
this module will give rudimentary support for handlebarsjs, jadejs
hamljs
###


cons = require 'consolidate'

documentRoot = System.getProperty 'gravy.viewRoot'
renderUri = conf 'view.renderUri'
engine = conf 'engine'
cache = conf 'cache'

get renderUri, (req, res) ->

	model = req.get '_model'
	viewUri = req.getAttribute '_view' 	# not serialized
	module = req.getAttribute '_module'	# not serialized
	moduleName = module.name
	res.contentType = 'text/html'

	if ( cache ) model.cache = true

	cons[engine] documentRoot+'/'+moduleName+viewUri, model, (err, html) ->
		if (err) throw err
		res.write html

