=begin

This script defines the Gravy API for Ruby

Note: all tokens with a 'j_' prefix are injected or passed into the ruby runtime from java 

=end

#gravy_initialized = true
  
require 'date' 
require 'json'
require 'yaml'
require 'ostruct'
require 'java'

java_import javax.servlet.DispatcherType
java_import org.microsauce.gravy.lang.object.CommonObject
java_import java.util.ArrayList
java_import java.util.HashMap
java_import java.lang.String
java_import org.microsauce.gravy.lang.object.GravyType
java_import org.microsauce.gravy.context.ruby.RubyHandler
java_import org.jruby.javasupport.JavaUtil

scope = self

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
    if self.is_a? Hash
      return self.each {|k,v| hash[k] = v.to_serializable()}
    else
      self.instance_variables.each do |var|
        element_value = instance_variable_get(var)
        hash[var[1, var.length].to_sym] = element_value.to_serializable
      end
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
    return nil if str.nil? or str.empty? 
    begin
      return JSON.parse(str).to_ostruct_recursive()
    rescue JSON::ParserError # this may be a primitive type
      return nil if str.nil? or str.empty? 
      return JSON.parse("{\"data\" : #{str} }")['data']
    end
  end
  def to_string obj
    return  obj.to_serializable.to_json
  end
end

# instantiate the serializers
serializer = Serializer.new


module GravyModule

  #
  # gravy constants
  #
  REQUEST = DispatcherType::REQUEST
  FORWARD = DispatcherType::FORWARD
  ERROR   = DispatcherType::ERROR
  INCLUDE = DispatcherType::INCLUDE

  #
  # define the scripting api
  #

  def get(uri_pattern, dispatch = [], &block)
    add_service uri_pattern, 'get', dispatch, &block
  end
  
  def post(uri_pattern, dispatch = [], &block)
    add_service uri_pattern, 'post', dispatch, &block
  end
  
  def put(uri_pattern, dispatch = [], &block)
    add_service uri_pattern, 'put', dispatch, &block
  end
  
  def delete(uri_pattern, dispatch = [], &block)
    add_service uri_pattern, 'delete', dispatch, &block
  end

  def route(uri_pattern, dispatch = [], &block)
    add_service uri_pattern, 'default', dispatch, &block
  end

  def use(uri_pattern, dispatch = [], &block)
    add_service uri_pattern, 'default', dispatch, &block
  end

  def schedule(cron_string, &block)
    add_scheduled_task cron_string, &block
  end

  private

  # private module classes

  class CallbackWrapper
    attr_accessor :uri_pattern, :method, :block

    def initialize(uri_pattern, method, &block)
      @uri_pattern = uri_pattern
      @method = method
      @block = block
    end

    # call this method from Java handler
    def invoke(req, res, param_map, param_list, object_binding, parms)
      ruby_handler = NativeRubyHandler.new(&@block)
      ruby_handler.invoke_handler req, res, param_map, param_list, object_binding, parms
    end

  end

  class ScheduledTaskCallbackWrapper
    attr_accessor :call_back

    def initialize(&call_back)
      @call_back = call_back
    end

    # call this method from Java handler
    def invoke(args)
      @call_back.call *args
    end

  end

  class NativeRubyHandler

    attr_accessor :block

    def initialize(&block)
      @block = block
    end

    def invoke_handler(req, res, param_map, param_list, object_binding, parms)

      _module = req.get_attribute('_module')
      binding = req.get_attribute('_ruby_binding_'+_module.name)

      if binding.nil?

          # add the req and res to the handler scope
          self.define_attribute 'req', req
          self.define_attribute 'res', res

          # add uri parameters to 'self'
          if not param_map.nil?
            iterator = param_map.keySet().iterator()
            while iterator.hasNext()
              key = iterator.next()
              self.define_attribute key, param_map.get(key)
            end

          end # if

          # create the 'splat' array
          if not param_list.nil? # TODO not equal or not nil
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

          # create the query/form binding
          method = req.getMethod
          #parameters = load_parameters(req)
          if method == 'GET' or method == 'DELETE'
            self.define_attribute 'query', OpenStruct.new(parms) #parameters
          elsif method == 'POST' or method == 'PUT'
            self.define_attribute 'form', OpenStruct.new(parms)  #parameters
          end

          req.set_attribute '_ruby_binding_'+_module.name, self
      end # if binding.nil?

      params = []
      param_iterator = param_list.iterator
      while param_iterator.has_next
        params << param_iterator.next
      end

      # call the handler
      (binding.nil? ? self : binding).instance_exec *params, &block

      res.out.flush()

    end

    def load_parameters(req)
      parameter_hash = {}
      req.getParameterMap.each do |k,v|
        parameter_hash[k] = v[0]
      end
      return OpenStruct.new(parameter_hash)
    end

    def to_s
      @uri_pattern
    end

  end

  # private methods

  def config_to_ostruct_recursive(config)
    result = config
    if result.java_kind_of? (Java::GroovyUtil::ConfigObject)
      result.each do |key, val|
        result[key] = config_to_ostruct_recursive(val)
      end
      result = OpenStruct.new result
    end
    return result
  end

  def add_service(uri_pattern, method, dispatch, &block)
    dispatch_list = ArrayList.new
    if dispatch.length == 0
      dispatch_list.add(REQUEST)
      dispatch_list.add(FORWARD)
    else
      for i in 0...dispatch.length
        dispatch_list.add(dispatch[i])
      end
    end

    call_back = CallbackWrapper.new(uri_pattern, method, &block)
    @j_module.addEnterpriseService(uri_pattern, method, call_back, dispatch_list)

  end

  def add_scheduled_task(cron_string, &call_back)
    @j_module.addCronService(cron_string, ScheduledTaskCallbackWrapper.new(&call_back))
  end

end
