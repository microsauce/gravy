=begin

This script defines the Gravy API for Ruby

Note: all tokens with a 'j_' prefix are injected into the ruby runtime from java 

=end

require 'json'
require 'ostruct'
require 'java'

java_import javax.servlet.DispatcherType
java_import org.microsauce.gravy.lang.object.CommonObject
java_import java.util.ArrayList
java_import java.util.HashMap

#TODO verify
ENV["RUBYLIB"] = j_mod_lib_path
ENV["GEM_HOME"] = j_gem_home

scope = self
services = OpenStruct.new

REQUEST = DispatcherType::REQUEST
FORWARD = DispatcherType::FORWARD
ERROR   = DispatcherType::ERROR
INCLUDE = DispatcherType::INCLUDE

# patch Object
class Object

  def to_hash
    hash = Hash.new
    self.instance_variables.each do |element|
        element_value = instance_variable_get(element)
        value = nil
        if element_value.is_a? Numeric or element_value.is_a? String or element_value.is_a? Date
          value = element_value
      else 
        value = element_value.to_hash
      end
        hash[element[1, element.length].to_sym] = value
    end

    return hash
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
    return  obj.to_hash.to_json
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

class Object
  
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
  
  def self.init(add_service)
    @@add_service = add_service
  end

  #
  # define the scripting api
  #

  def get(uri_pattern, dispatch = [], &block)
    @@add_service.call uri_pattern, 'get', dispatch, &block
  end
  
  def conf(key)
    return j_properties.getProperty key
  end

end

GravyModule.init(add_service)

include GravyModule

# TODO convert to Ruby
class Imports
  def initialize(import_map)
    import_map.each do |k, v|
#      self.define_attribute k, v
#      self.define_attribute 'handler', v
      @handler = v
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
