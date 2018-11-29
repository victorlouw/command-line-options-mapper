package command_line_options_mapper.core.test_model;

import command_line_options_mapper.core.mapper.CommandLineOption;

public class ApiConfig {
	
	private String baseUrl;
	private String projectId;
	private String username;
	private String password;
	
	public ApiConfig() {}
	
	public ApiConfig(
		String baseUrl,
		String projectId,
		String username,
		String password
	) {
		this.baseUrl = baseUrl;
		this.projectId = projectId;
		this.username = username;
		this.password = password;
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
	
	@CommandLineOption(
		name = "p",
		longName = "projectId",
		description = "Project ID of project",
		required = true,
		requiresArgument = true
	)
	public String getProjectId() {
		return projectId;
	}

	@CommandLineOption(
		name = "u",
		longName = "username",
		description = "Username for API authentication",
		required = true,
		requiresArgument = true
	)
	public String getUsername() {
		return username;
	}

	@CommandLineOption(
		name = "v",
		longName = "password",
		description = "Password for API authentication",
		required = true,
		requiresArgument = true
	)
	
	public String getPassword() {
		return password;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "ApiConfig [baseUrl=" + baseUrl + ", projectId=" + projectId + ", username=" + username + ", password="
			+ password + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseUrl == null) ? 0 : baseUrl.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApiConfig other = (ApiConfig) obj;
		if (baseUrl == null) {
			if (other.baseUrl != null)
				return false;
		} else if (!baseUrl.equals(other.baseUrl))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (projectId == null) {
			if (other.projectId != null)
				return false;
		} else if (!projectId.equals(other.projectId))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

}
