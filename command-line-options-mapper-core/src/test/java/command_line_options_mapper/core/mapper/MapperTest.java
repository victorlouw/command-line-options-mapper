package command_line_options_mapper.core.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import command_line_options_mapper.core.test_model.ApiConfig;

class MapperTest {

	@Test
	void test() {
		
		String
			baseUrl = "http://xyz.com",
			projectId = "pets",
			username = "root",
			password = "admin123";
		
		ApiConfig expected = new ApiConfig(baseUrl, projectId, username, password);
		
		String args = String.format("--baseUrl %s -p %s -u %s -v %s",
			baseUrl, projectId, username, password);
		ApiConfig actual = Mapper.map(ApiConfig.class, args.split(" "));
		
		assertEquals(expected, actual);
		
	}

}
