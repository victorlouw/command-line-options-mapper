package command_line_options_mapper.core.mapper;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandLineOption {

	/**
	 * Synonym for {@link #name()}
	 * @return
	 */
	String value() default "";
	
	/**
	 * Short name of the option.
	 * @return
	 */
	String name() default "";
	
	String longName() default "";
	
	String description() default "";
	
	boolean required() default false;
	
	/**
	 * Option requires exactly one argument.
	 * @return
	 */
	boolean requiresArgument() default false;

	/**
	 * Option can have zero or one arguments.
	 * @return
	 */
	boolean hasOptionalArgument() default false;
	
	boolean hasArguments() default false;
	
	int numberOfArguments() default -1;
	
}
