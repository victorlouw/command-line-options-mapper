package command_line_options_mapper.core.mapper;

public interface Mapper<T> {

	T map(String... arguments);

	public static <T> T map(Class<T> type, String... args) {
		return
		MapperCreator.createDefault()
			.createMapper(type)
			.map(args);
	}
}
