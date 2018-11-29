package command_line_options_mapper.core.mapper;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Option.Builder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.collect.ImmutableSet;

public class MapperCreator {

	public static MapperCreator createDefault() {
		return new MapperCreator(
			OptionValueGetterFactories.createDefault(),
			new PropertyUtils()
		);
	}
	
	private OptionValueGetterFactories optionValueGetterFactories;
	private PropertyUtils propertyUtils;
	
	public MapperCreator(
		OptionValueGetterFactories optionValueGetterFactories,
		PropertyUtils propertyUtils
	) {
		this.optionValueGetterFactories = optionValueGetterFactories;
		this.propertyUtils = propertyUtils;
	}
	
	public <T> Mapper<T> createMapper(Class<T> type) {

		List<OptionInfo> info =
			asList(type.getMethods()).stream()
				.map(m -> createOptionInfo(m, type))
				.filter(Optional::isPresent).map(Optional::get)
				.collect(toList());
		
		Options options = new Options();
		info.stream()
			.map(OptionInfo::getOption)
			.forEach(options::addOption);
		
		List<OptionProcessor> processors =
			info.stream()
				.map(OptionInfo::getProcessor)
				.collect(toList());
		
		CommandLineParser parser = new DefaultParser();

		return new Mapper<T>() {

			@Override
			public T map(String... arguments) {
				try {
					T instance = type.newInstance();
					CommandLine cmd = parser.parse(options, arguments);
					processors.forEach(p -> p.apply(cmd, instance));
					return instance;
				}
				catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException("could not instantiate " + type, e);
				}
				catch (ParseException e) {
					throw new RuntimeException("error parsing command line arguments", e);
				}
			}
		};
	}
	
	private Optional<OptionInfo> createOptionInfo(Method method, Class<?> type) {
		
		CommandLineOption annotation = method.getAnnotation(CommandLineOption.class);
		if (annotation == null) return empty();
		
		Option option = createOptionFromAnnotation(annotation);
		String name = option.getOpt();

		Method setter = propertyUtils.findSetter(method, type);
		
		return Optional.of(
			new OptionInfo(
				createOptionProcessor(setter, name),
				option
			)
		);
	}
	
	private BiConsumer<Object, Object> createSetterInvoker(Method setter) {
		return (o, v) -> {
			try {
				setter.invoke(o, v);
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("failed to invoke setter " + setter +
					" on instance " + o + " with value " + v);
			}
		};
	}
	
	private Consumer<Object> createZeroArgumentSetterInvoker(Method setter) {
		return o -> {
			try {
				setter.invoke(o);
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("failed to invoke setter " + setter +
					" on instance " + o + " (no args)");
			}
		};		
	}
	
	private OptionProcessor createOptionProcessor(Method setter, String name) {
		
		Type[] parameterTypes = setter.getGenericParameterTypes();
		if (parameterTypes.length == 1) {
			
			Function<CommandLine, Optional<Object>> getValue;
			
			Type optionType = setter.getGenericParameterTypes()[0];
			if (optionType instanceof ParameterizedType) {
				ParameterizedType pt = (ParameterizedType) optionType;
				Class<?> raw = (Class<?>) pt.getRawType();
				if (Iterable.class.isAssignableFrom(raw))
					throw new RuntimeException("parameterized type " + optionType + 
						" has a raw type that is not a sub type of Iterable");
				Class<?> iterableType = raw;
				Class<?> elementType = (Class<?>) pt.getActualTypeArguments()[0];
				checkPropertyTypeAllowed(elementType);
				
				getValue = optionValueGetterFactories
					.createOptionCollectionValueGetter(iterableType, elementType, name);
			}
			
			else if (optionType instanceof Class) {
				checkPropertyTypeAllowed((Class<?>) optionType);
				getValue = optionValueGetterFactories
					.createOptionValueGetter((Class<?>) optionType, name);
			}
			
			else
				throw new RuntimeException("type of setter parameter [" + optionType + "] "
					+ "is not a ParameterizedType or Class");
			
			BiConsumer<Object, Object> invokeSetter = createSetterInvoker(setter);
			return (c, o) -> {
				getValue.apply(c)
					.ifPresent(v ->
						invokeSetter.accept(o, v));
			};
		}
		
		// no parms. invoke setter if the option is present
		Consumer<Object> invokeSetter = createZeroArgumentSetterInvoker(setter);
		return (c, o) -> {
			if (c.hasOption(name))
				invokeSetter.accept(o);
		};
	}
	
	// TODO this should actually depend on the configured/available value adapters.
	// if we have a value adapter String -> Person, why not allow it?
	private final static Set<Class<?>> allowedPropertyTypes =
		ImmutableSet.of(String.class, Integer.class, Long.class, Boolean.class);
	
	private void checkPropertyTypeAllowed(Class<?> type) {
		if (allowedPropertyTypes.stream()
			.noneMatch(i -> i.isAssignableFrom(type))
		)
			throw new RuntimeException("setter parameter type or element type [" + type + "]"
				+ " is not one of allowed types " + allowedPropertyTypes);
	}
	
	private Option createOptionFromAnnotation(CommandLineOption annotation) {
		
		String name = annotation.value();
		if (name.isEmpty())
			name = annotation.name();
		if (name.isEmpty())
			throw new RuntimeException("@" + CommandLineOption.class.getName() + ".name or .value must be non-empty");
		
		Builder builder = Option.builder(name);
		
		String longName = annotation.longName();
		if (!longName.isEmpty())
			builder.longOpt(longName);

		String description = annotation.description();
		if (!description.isEmpty())
			builder.desc(description);
		
		if (annotation.required())
			builder.required();
		
		if (annotation.requiresArgument())
			builder.hasArg();
		
		if (annotation.hasOptionalArgument())
			builder.optionalArg(true);
		
		if (annotation.hasArguments())
			builder.hasArgs();
		
		int argCount = annotation.numberOfArguments();
		if (argCount >= 0)
			builder.numberOfArgs(argCount);
		
		return builder.build();
	}
	
}
