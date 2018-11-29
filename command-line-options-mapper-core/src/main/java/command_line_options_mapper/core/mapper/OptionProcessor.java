package command_line_options_mapper.core.mapper;

import org.apache.commons.cli.CommandLine;

interface OptionProcessor {
	
	void apply(CommandLine commandLine, Object instance);
	
}