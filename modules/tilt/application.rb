
#
# TODO - 0.2
#   add support for layouts and partials
#

# java imports
java_import java.lang.System
java_import java.util.concurrent.ConcurrentHashMap
java_import java.util.HashMap

# module configuration
engine = conf 'engine'
render_uri = conf 'view.renderUri'
document_root = System.getProperty 'gravy.viewRoot' 

# ruby imports
require 'ostruct'
require engine
require 'tilt'

# template cache
cache = ConcurrentHashMap.new 

load_template = proc { |template_path| 

  template = cache[template_path]
  if template.nil?
    template = Tilt.new(template_path)
    cache[template_path] = template
  end
  
  template
}

# define the service 
get render_uri do
  model = req.get '_model'
  view_uri = req.getAttribute '_view'  # not serialized
  _module = req.getAttribute '_module' # not serialized
  res.contentType = 'text/html'

  template = load_template.call document_root+'/'+_module.getName+'/'+view_uri

  if model.is_a? Hash
    # it will always be an ostruct
    res.write template.render nil, model 
  else
    res.write(template.render(model))
  end
  #res.flush # TODO
end

