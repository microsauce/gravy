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
require 'set'

java_import javax.servlet.http.HttpServletRequest
java_import javax.servlet.http.HttpServletResponse
java_import javax.servlet.http.HttpSession
java_import org.microsauce.gravy.lang.object.SharedObject
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
    if self.respond_to? :capitalize or self.is_a? (Numeric)   # TODO there appears to be a JRuby bug/issue here, resort to duck-type check for String
      return self
    elsif self.is_a? (Date)
      return self.new_offset(0)
    elsif self.is_a? (Array)
      return self.collect {|element| element.to_serializable}
    end

    hash = Hash.new
    if self.is_a? (Hash)
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
  # define the scripting api
  #

  def get(uri_pattern, *call_backs, &block)
puts "get: call_backs: #{call_backs}"
    add_service uri_pattern, 'get', call_backs, block
  end
  
  def post(uri_pattern, *call_backs, &block)
    add_service uri_pattern, 'post', call_backs, block
  end
  
  def put(uri_pattern, *call_backs, &block)
    add_service uri_pattern, 'put', call_backs, block
  end
  
  def delete(uri_pattern, *call_backs, &block)
    add_service uri_pattern, 'delete', call_backs, block
  end

  def all(uri_pattern, *call_backs, &block)
    add_service uri_pattern, 'get', call_backs, block
    add_service uri_pattern, 'post', call_backs, block
    add_service uri_pattern, 'put', call_backs, block
    add_service uri_pattern, 'delete', call_backs, block
  end

  def route(uri_pattern, *call_backs, &block)
    add_service uri_pattern, 'default', call_backs, block
  end

  def use(uri_pattern, *call_backs, &block)
    add_service uri_pattern, 'default', call_backs, block
  end

  def param(param_name, &block)
    call_back = CallbackWrapper.new('', 'default', block)
    @j_module.addParameterPrecondition(param_name, call_back)
  end

  def schedule(cron_string, &block)
    add_scheduled_task cron_string, block
  end

  private

  # private module classes

  class CallbackWrapper
    attr_accessor :uri_pattern, :method, :call_back

    def initialize(uri_pattern, method, call_back)
      @uri_pattern = uri_pattern
      @method = method
      @call_back = call_back
    end

    # call this method from Java handler
    def invoke(servlet_facade)
      ruby_handler = NativeRubyHandler.new(@call_back)
      ruby_handler.invoke_handler servlet_facade
    end

  end

  class ScheduledTaskCallbackWrapper
    attr_accessor :call_back

    def initialize(call_back)
      @call_back = call_back
    end

    # call this method from Java handler
    def invoke(args)
      @call_back.call *args
    end

  end

  class RubySession
    @@session_methods = nil
    def initialize(servlet_facade)
      @servlet_facade = servlet_facade
      @j_session = servlet_facade.session
      @sess_methods = get_sess_methods(@j_session)
    end
    def method_missing(name, *args, &block)
       if @sess_methods.member? name
         @j_session.send(name, *args, &block)   # or java_send
       else
         attr_name = name
         if name.to_s.end_with?('=')  # this is a write
           attr_name = name[0,name.size-1]
           return @servlet_facade.set_session_attr attr_name, *args
         else # this is a read
           return @servlet_facade.get_session_attr attr_name
         end
       end
    end
    private
    def get_sess_methods(sess)
      if @@session_methods.nil?
        @@session_methods = sess.class.public_instance_methods
      end
      return @@session_methods
    end
  end
  class RubyRequest
    @@request_methods = nil

    attr_accessor :input, :session, :json #, :form, :query
    def initialize(servlet_facade)
      @servlet_facade = servlet_facade
      @j_request = servlet_facade.native_req
      @req_methods = get_req_methods(@j_request)
      @input = servlet_facade.input # TODO
      @session = get_ruby_session
      @json = servlet_facade.get_json
    end
    def method_missing(name, *args, &block)
       if @req_methods.member? name
         return @j_request.send(name, *args, &block)   # or java_send
       else
         attr_name = name
         if name.to_s.end_with?('=')  # this is a write
           attr_name = name[0,name.size-1]
           return @servlet_facade.set_attr attr_name, *args  #@j_request.set_attribute attr_name, *args
         else # this is a read
           return @servlet_facade.get_attr attr_name #@j_request.get_attribute attr_name
         end
       end
    end
    def next
      @servlet_facade.next
    end
    def forward(uri)
      @servlet_facade.forward uri
    end
    private
    def get_ruby_session
      j_session = @j_request.session
      ruby_session = j_session.get_attribute '_ruby_session'
      if ruby_session.nil?
        ruby_session = RubySession.new @servlet_facade
        j_session.set_attribute '_ruby_session', ruby_session
      end
      return ruby_session
    end
    def get_req_methods(req)
      if @@request_methods.nil?
        @@request_methods = req.class.public_instance_methods
      end
      return @@request_methods
    end
  end
  class RubyResponse
    attr_accessor :out
    def initialize(servlet_facade)
      @servlet_facade = servlet_facade
      @j_response = servlet_facade.native_res
      @out = servlet_facade.out
    end
    def method_missing(name, *args, &block)
      return @j_response.send(name, *args, &block)
    end

    def print(str)
      @servlet_facade.print str
    end
    def write(bytes)
      @servlet_facade.write bytes
    end
    def redirect(url)
      @servlet_facade.redirect url
    end
    def renderJson(model)
      @servlet_facade.render_json model
    end
    def render(viewUri, model)
      @servlet_facade.render viewUri, model
    end
  end
  class RubyReqParams
    def initialize(j_map)
      @j_map = j_map
    end
    def method_missing(name, *args, &block)
      if name.to_s.end_with?('=')  # this is a write
        attr_name = name[0,name.size-1]
        return @j_map.put name.to_s, args[0]
      else # this is a read
        return @j_map.get name.to_s
      end
    end
  end
  class NativeRubyHandler

    attr_accessor :call_back

    def initialize(call_back)
      @call_back = call_back
    end

    def invoke_handler(servlet_facade) #req, res, param_map, param_list, object_binding, parms)

      ruby_facade = servlet_facade.native_req.get_attribute('_ruby_facade')

      if ruby_facade.nil?

        req = RubyRequest.new servlet_facade
        res = RubyResponse.new servlet_facade

        # add the req and res to the handler scope
        self.define_attribute 'req', req
        self.define_attribute 'res', res

        # add uri parameters to 'self'
        if not servlet_facade.uri_param_map.nil?
          iterator = servlet_facade.uri_param_map.keySet().iterator()
          while iterator.hasNext()
            key = iterator.next()
            self.define_attribute key, servlet_facade.uri_param_map.get(key)
          end
        end # if

        # create the 'splat' array
        if not servlet_facade.splat.nil? # TODO not equal or not nil
          iterator = servlet_facade.splat.iterator()
          splat = Array.new
          while iterator.hasNext()
            splat.push(iterator.next())
          end
          self.define_attribute 'splat', splat
        end

        # create the query/form binding
        method = servlet_facade.native_req.getMethod
        request_parameters = RubyReqParams.new(servlet_facade.request_params)
        if method == 'GET' or method == 'DELETE'
          self.define_attribute 'query', request_parameters #parameters
        elsif method == 'POST' or method == 'PUT'
          self.define_attribute 'form', request_parameters  #parameters
        end

        req.set_attribute '_ruby_facade', self
        ruby_facade = self
      end # if binding.nil?

      params = []
      splat_iterator = servlet_facade.splat.iterator
      while splat_iterator.has_next
        params << splat_iterator.next
      end

      # call the handler
      ruby_facade.instance_exec *params, &@call_back
#      servlet_facade.out.flush

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
  def add_service(uri_pattern, method, call_backs, block)
    if call_backs.length == 0 and block.nil?
      raise "ERROR: no callbacks were defined for route: #{method} - #{uri_pattern}"
    end
    middleware = nil
    end_point = nil
    if block.nil?
      if call_backs.length == 1
        middleware = []
        end_point = call_backs[0]
      else
        middleware = call_backs[0,call_backs.length-1]
        end_point = call_backs[call_backs.length-1]
      end
    else
      middleware = call_backs
      end_point = block
    end

    end_point_wrapper = CallbackWrapper.new(uri_pattern, method, end_point)
    middleware_wrappers = ArrayList.new
    middleware.each do |this_callback|
      middleware_wrappers.add(CallbackWrapper.new('', 'default', this_callback))
    end
    @j_module.addEnterpriseService(uri_pattern, method, middleware_wrappers, end_point_wrapper)
  end

  def add_scheduled_task(cron_string, call_back)
    @j_module.addCronService(cron_string, ScheduledTaskCallbackWrapper.new(call_back))
  end

end
