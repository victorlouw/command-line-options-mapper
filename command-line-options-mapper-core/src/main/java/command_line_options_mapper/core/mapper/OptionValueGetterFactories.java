package command_line_options_mapper.core.mapper;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.cli.CommandLine;

import com.google.common.collect.ImmutableMap;

class OptionValueGetterFactories {

	static OptionValueGetterFactories createDefault() {
		
		return new OptionValueGetterFactories(
				
			ImmutableMap.<Class<?>, Supplier<Collection<Object>>>builder()
				.put(Set.class, LinkedHashSet::new)
				.put(List.class, ArrayList::new)
				.build(),
				
			ImmutableMap.<Class<?>, Function<String, Object>>builder()
				.put(String.class, v -> v)
				.put(Integer.class, Integer::parseInt)
				.put(Long.class, Long::parseLong)
				.put(Boolean.class, Boolean::parseBoolean)
				.build()
		);
	}

	private Map<Class<?>, Supplier<Collection<Object>>> collectionFactories;
	private Map<Class<?>, Function<String, Object>> valueAdapters;
	
	OptionValueGetterFactories(
		Map<Class<?>, Supplier<Collection<Object>>> collectionFactories,
		Map<Class<?>, Function<String, Object>> valueAdapters
	) {
		this.collectionFactories = collectionFactories;
		this.valueAdapters = valueAdapters;
	}
	
	private static <K, V> Optional<V> getOptional(Map<K, V> x, K key) {
		return x.containsKey(key)
			? Optional.of(x.get(key))
			: empty();
	}
	
	private Optional<Supplier<Collection<Object>>> getCollectionFactory(Class<?> type) {
		return getOptional(collectionFactories, type);
	}
	
	private Optional<Function<String, Object>> getValueAdapter(Class<?> type) {
		return getOptional(valueAdapters, type);
	}
	
	private Function<String, Object> getValueAdapterOrFail(Class<?> type) {
		return getValueAdapter(type)
			.orElseThrow(() ->
				new RuntimeException("no value adapter present to adapt values to type " + type));
	}
	
	Function<CommandLine, Optional<Object>> createOptionCollectionValueGetter(
		Class<?> collectionType,
		Class<?> elementType,
		String optionName
	) {

		Supplier<Collection<Object>> createCollection =
			getCollectionFactory(collectionType)
				.orElseThrow(() ->
					new RuntimeException("no collection factory present for type " + collectionType));

		Function<String, Object> transformer = getValueAdapterOrFail(elementType);

		return c -> {
			
			String[] optionValues = c.getOptionValues(optionName);
			if (optionValues == null) return empty();
			
			Collection<Object> result = createCollection.get();
			
			asList(optionValues).stream()
				.map(transformer)
				.forEach(result::add);
			
			return Optional.of(result);
		};
	}
	
	Function<CommandLine, Optional<Object>> createOptionValueGetter(Class<?> optionType, String optionName) {
		
		Function<String, Object> adapter = getValueAdapterOrFail(optionType);
		
		return c -> {
			String value = c.getOptionValue(optionName);
			if (value == null) return empty();
			return Optional.of(adapter.apply(value));
		};		
	}
	
}
