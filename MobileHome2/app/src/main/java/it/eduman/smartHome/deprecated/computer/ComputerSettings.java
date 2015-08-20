package it.eduman.smartHome.deprecated.computer;

public class ComputerSettings {
	private String url;
	private String linksmartDescription;
	private String password;
	private String description;
	
	public ComputerSettings(String url, String linksmartDescription, String password, String description){
		this.linksmartDescription = linksmartDescription;
		this.url = url;
		this.password = password;
		this.description = description;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getLinksmartDescription() {
		return linksmartDescription;
	}
	public void setLinksmartDescription(String linksmartDescription) {
		this.linksmartDescription = linksmartDescription;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	
}
