package command_line_options_mapper.core.mapper;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

class PropertyUtils {

	Method findSetter(Method method, Class<?> type) {
		String name = method.getName();
		String property = getPropertyName(name);
		String setterName = createSetterName(property);
		List<Method> setters =
			asList(type.getMethods()).stream()
				.filter(m -> m.getName().equals(setterName))
				.filter(m -> m.getParameterCount() <= 1)
				.collect(toList());
		if (setters.isEmpty())
			throw new RuntimeException("found no setters for property " +
				"[" + property + "] from annotated method " + name);
		if (setters.size() > 1)
			throw new RuntimeException("found multiple setters for property " +
				"[" + property + "] from annotated method " + name);
		return setters.get(0);
	}
	
	private String createSetterName(String property) {
		if (property.isEmpty()) return null;
		String head = property.substring(0, 1);
		String tail = property.substring(1);
		return "set" + head.toUpperCase() + tail;
	}
	
	private final Set<String> propertyGetterSetterPrefixes = ImmutableSet.of("get", "set", "is");
	
	private String getPropertyName(String name) {
		return
		propertyGetterSetterPrefixes.stream()
			.map(p -> getPropertyName(name, p))
			.filter(Optional::isPresent).map(Optional::get)
			.findFirst()
			.orElse(name);
	}
	
	private Optional<String> getPropertyName(String methodName, String prefix) {
		if (!methodName.startsWith(prefix)) return empty();
		String remainder = methodName.substring(prefix.length());
		if (remainder.isEmpty()) return empty();
		String head = remainder.substring(0, 1);
		if (!head.toUpperCase().equals(head)) return empty(); // head must be upper case
		String tail = remainder.substring(1);
		return Optional.of(head.toLowerCase() + tail);
	}
	
}
