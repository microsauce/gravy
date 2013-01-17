=begin

This script defines the Gravy API for Ruby

Note: all tokens with a 'j_' prefix are injected or passed into the ruby runtime from java 

=end

require 'date' 
require 'json'
require 'ostruct'
require 'java'

java_import javax.servlet.DispatcherType
java_import org.microsauce.gravy.lang.object.CommonObject
java_import java.util.ArrayList
java_import java.util.HashMap

#TODO verify
#ENV["RUBYLIB"] = j_mod_lib_path
#ENV["GEM_HOME"] = j_gem_home

scope = self
services = OpenStruct.new

REQUEST = DispatcherType::REQUEST
FORWARD = DispatcherType::FORWARD
ERROR   = DispatcherType::ERROR
INCLUDE = DispatcherType::INCLUDE

# patch Object
class Object

  def to_serializable 
    if self.is_a? Numeric or self.is_a? String
      return self
    elsif self.is_a? Date
      return self.new_offset(0)
    elsif self.is_a? Array
      return self.collect {|element| element.to_serializable}
    end

    hash = Hash.new
    self.instance_variables.each do |var|
      element_value = instance_variable_get(var)
      hash[var[1, var.length].to_sym] = element_value.to_serializable
    end
    return hash
  end
  
  def metaclass
    class << self
      self
    end
  end
  
  def define_attributes(hash)
    hash.each_pair { |key, value|
      metaclass.send :attr_accessor, key
      send "#{key}=".to_sym, value
    }
  end
  
  def define_attribute(key, value)
    metaclass.send :attr_accessor, key
    send "#{key}=".to_sym, value
  end

end

# patch the hash
class Hash

  def to_ostruct_recursive()
    convert_to_ostruct_recursive(self) 
  end

  def with_sym_keys
    self.inject({}) { |memo, (k,v)| memo[k.to_sym] = v; memo }
  end
 
  private
  def convert_to_ostruct_recursive(obj)
    result = obj
    if result.is_a? Hash
      result = result.dup.with_sym_keys
      result.each  do |key, val| 
        result[key] = convert_to_ostruct_recursive(val)
      end
      result = OpenStruct.new result       
    elsif result.is_a? Array
       result = result.collect { |r| convert_to_ostruct_recursive(r) }
    end
    if obj.is_a? String and obj.length >= 19
      sub_str = obj[0,20]
      if sub_str =~ /[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}/
        result = DateTime.strptime(sub_str, '%Y-%m-%dT%H:%M:%S').new_offset(0)
      end
    end
    return result
  end
  
end

#
# define the Ruby serializer
#
class Serializer
  def parse str
    return JSON.parse(str).to_ostruct_recursive()
  end
  def to_string obj
    return  obj.to_serializable.to_json
  end
end

# instantiate the serializer 
serializer = Serializer.new

class CallbackWrapper
  attr_accessor :uri_pattern, :method, :block

  def initialize(uri_pattern, method, &block)
    @uri_pattern = uri_pattern
    @method = method
    @block = block
  end

  # call this method from Java handler
  def invoke(req, res, param_map, param_list, object_binding)
    ruby_handler = RubyHandler.new(&@block)
    ruby_handler.invoke_handler req, res, param_map, param_list, object_binding
  end
  
end


class RubyHandler

  attr_accessor :block 
  
  def initialize(&block)
    @block = block
  end

  def invoke_handler(req, res, param_map, param_list, object_binding)

    # add the req and res to the handler scope
    self.define_attribute 'req', req
    self.define_attribute 'res', res

    # add uri parameters to 'self'
    if param_map != nil
      iterator = param_map.keySet().iterator()
      while iterator.hasNext()
        key = iterator.next()
        self.define_attribute key, param_map.get(key)
      end

    end # if

    # create the 'splat' array
    if param_list != nil # TODO not equal or not nil
      iterator = param_list.iterator()
      splat = Array.new
      while iterator.hasNext()
        splat.push(iterator.next())
      end
      self.define_attribute 'splat', splat
    end

    # add object bindings
    if object_binding != nil 
      iterator = object_binding.keySet().iterator()
      while iterator.hasNext()
        key = iterator.next()
        self.define_attribute key, object_binding.get(key)
      end
    end

    # call the handler
    self.instance_exec &block

  end
 
  def to_s
    @uri_pattern
  end

end

add_service = Proc.new { |uri_pattern, method, dispatch, &block|
  dispatch_list = ArrayList.new
  if dispatch.length == 0 
    dispatch_list.add(REQUEST)
    dispatch_list.add(FORWARD)
  else
    for i in 0...dispatch.length
      dispatch_list.add(dispatch[i])
    end
  end

  call_back = CallbackWrapper.new(uri_pattern, 'GET', &block)
  j_gravy_module.addEnterpriseService(uri_pattern, method, call_back, dispatch_list)
  
}


module GravyModule
  
  def self.init(add_service, conf)
    @@add_service = add_service
    @@conf = conf
  end

  #
  # define the scripting api
  #

  def get(uri_pattern, dispatch = [], &block)
    @@add_service.call uri_pattern, 'get', dispatch, &block
  end
  
  def post(uri_pattern, dispatch = [], &block)
    @@add_service.call uri_pattern, 'post', dispatch, &block
  end
  
  def put(uri_pattern, dispatch = [], &block)
    @@add_service.call uri_pattern, 'put', dispatch, &block
  end
  
  def delete(uri_pattern, dispatch = [], &block)
    @@add_service.call uri_pattern, 'delete', dispatch, &block
  end

  def route(uri_pattern, dispatch = [], &block)
    @@add_service.call uri_pattern, 'default', dispatch, &block
  end
  
  def conf(key)
    return @@conf.getProperty key
  end

end

GravyModule.init(add_service, j_properties)

include GravyModule

class Imports
  def initialize(import_map) # TODO this is brokin
puts "\timport_map: #{import_map}"    
    import_map.each do |k, v|
#      self.define_attribute k, v
#      self.define_attribute 'handler', v
      @handler = v # TODO this looks broken
      self.define_attribute k, Proc do |parm_1,parm_2,parm_3,parm_4,parm_5,parm_6,parm_7|
        return @handler.call(
          common_obj(parm_1),
          common_obj(parm_2),
          common_obj(parm_3),
          common_obj(parm_4),
          common_obj(parm_5),
          common_obj(parm_6),
          common_obj(parm_7)
        )
      end

    end
  end
  
  def common_obj(obj) 
      return CommonObject.new(obj, GravyType.RUBY)
  end
end

class ImportExport
  def prepare_imports(j_all_imports, scope) 
puts "imports: #{j_all_imports}"    
    module_iterator = j_all_imports.entrySet().iterator() # TODO
    while module_iterator.hasNext() 
      this_module_exports = module_iterator.next()
      module_name = this_module_exports.getKey()
      module_imports = this_module_exports.getValue()
      scope.define_attribute module_name, Imports.new(module_imports) 
    end
  end
  
  def prepare_exports(exports)  # service exports 
    prepared_exports = HashMap.new
    exports.each do |this_export|
      if this_export.is_a? Proc
        var handler = RubyHandler.new(this_export) 
        prepared_exports.put(exp, handler)
      end
    end
    
    return prepared_exports
  end
  
end

import_export = ImportExport.new