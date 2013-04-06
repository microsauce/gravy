
# java imports
java_import java.lang.System
java_import java.util.concurrent.ConcurrentHashMap
java_import java.util.HashMap

# configure the module
engine = conf.engine
render_uri = conf.view.renderUri
document_root = System.get_property 'gravy.viewRoot'

# ruby imports
#require 'ostruct'
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
  view_uri = req.get_attribute '_view'  # not serialized
  _module = req.get_attribute '_module' # not serialized

  template = load_template.call document_root+'/'+_module.name+'/'+view_uri

  # TODO RE-TEST THIS - we will hit the else block now
  if model.is_a? Hash
    res.write template.render nil, model
  else
    res.write(template.render(model))
  end

end