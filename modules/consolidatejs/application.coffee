
load 'handlebars-1.0.rc.1.js'

documentRoot = "#{conf('gravy.appRoot')}/WEB-INF/view"
cache = {}

retrieveTemplate = (uri, moduleName) ->
	template = cache[uri]
	if ( template == null )
		source =  readFile "#{documentRoot}#{uri}"
		template = Handlebars.compile source
