package command_line_options_mapper.core.mapper;

import org.apache.commons.cli.Option;

class OptionInfo {
	
	OptionProcessor processor;
	Option option;
	
	public OptionInfo(OptionProcessor processor, Option option) {
		this.processor = processor;
		this.option = option;
	}
	
	public OptionProcessor getProcessor() {
		return processor;
	}
	
	public Option getOption() {
		return option;
	}
}