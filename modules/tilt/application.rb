# java imports
java_import java.lang.System

# module configuration
engine = conf 'engine'
render_uri = conf 'view.renderUri'
document_root = System.getProperty 'gravy.viewRoot' 

# ruby imports
require 'tilt'
require engine

# template cache
cache = ConcurrentHashMap.new # TODO import

load_template = proc { |module_name, uri|
  templates = cache[module_name]
  if mod == nil
    templates = ConcurrentHashMap.new
    cache[module_name] = templates
  end
  
  template = templates[uri]
  if template == nil
    template = Tilt.new(uri)
    templates[uri] = template
  end
  
  return template
}

get render_uri do
  model = req.get '_model'
  view_uri = req.getAttribute '_view'  # not serialized
  module_name = req.getAttribute '_module' # not serialized
  module_name = module.name
  res.contentType = 'text/html'

  template = load_template module_name, document_root+'/'+view_uri
  
  if model.is_a? Hash
    res.write(template.render(nil, model))
  else
    res.write(template.render(model))
  end
  res.flush
end
