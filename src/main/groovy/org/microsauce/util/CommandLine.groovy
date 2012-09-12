package org.microsauce.util

class CommandLine {
	
	def args
	
	CommandLine(String[] args) {
		this.args = args;
	}
	
	public boolean hasOption(String option) {
		for (String thisArg : args) {
			if (thisArg.equals(option) || thisArg.equals("-"+option))
				return true
		}
		
		return false
	}
	
	public String optionValue(String option) {
		
		for (int i = 0; i < args.length; i++) {
			String thisArg = args[i]
			if (thisArg.equals(option) || thisArg.equals("-"+option)) {
				//
				// does this option have a value
				//
				if (args.length > i+1) {
					if (!args[i+1].startsWith("-")) {
						return args[i+1]
					}
					else return null
				}
			}
		}
		
		return null
	}

	public List<String> listOptionValue(String option) {
		def value = [] as List<String> 
		for (int i = 0; i < args.length; i++) {
			String thisArg = args[i]
			if (thisArg.equals(option) || thisArg.equals("-"+option)) {
				//
				// does this option have a value
				//
				if (args.length >= i+1) {
					if (!args[i+1].startsWith("-")) {
						value << args[i+1]
					}
				}
			}
		}
		
		return value
	}
	
}
