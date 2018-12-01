# command-line-options-mapper

Modest library to map command-line arguments to an instance of a class. The mapping is controlled with annotations on the class. Internally, [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/) is used. Its way of configuring command-line options/arguments are reflected by the annotations of command-line-options-mapper.

## Example

We annotate properties on our "data class" to specify which command line option it corresponds with. In a real situation, you'll probably have a few or many properties annotated like this.

```java
public class ApiConfig {
	
	private String baseUrl;
	...
	
	public ApiConfig() {}
	
	public ApiConfig(String baseUrl, ... ) {
		this.baseUrl = baseUrl;
		...
	}

	@CommandLineOption(
		name = "b",
		longName = "baseUrl",
		description = "Base URL to execute API requests against",
		required = true,
		requiresArgument = true
	)
	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	...

}
```

Next, we invoke the mapper, specifying the data class and the command-line arguments as a `String` array.

```java
public static void main(String... args) {

	ApiConfig config = Mapper.map(ApiConfig.class, args);
	System.out.println(config.getBaseUrl());
}
```
