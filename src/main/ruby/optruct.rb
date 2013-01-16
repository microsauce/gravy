module Optruct

  def self.define(*syms, &block)
    o = Struct.new(*syms).new
    o.instance_eval do
      def set(h={})
        h.each do |k, v|
          raise "#{k} is not defined" unless members.include?(k)
          self[k] = v
        end
        self
      end
    end
    o.instance_eval &block if block_given?
    o
  end

  def optruct(*syms, &block)
    Optruct.define(*syms, &block)
  end

end

