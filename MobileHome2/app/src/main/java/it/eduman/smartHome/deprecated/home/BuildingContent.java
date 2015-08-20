package it.eduman.smartHome.deprecated.home;

import java.util.HashMap;

public class BuildingContent {
	private String buildingId;
	private String description = "Unknown Building";

	private HashMap<String, HomeContent> homeContentMap =  new HashMap<String, HomeContent>();
	private String address = "Unknown Address";
	private String cap = "Unknown cap";
	private String city = "Unknown City";
	private String nation = "Unknown Nation";
	
	public BuildingContent () {}
	public BuildingContent (String description) {
		this.description = description;
	}


	public BuildingContent setBuildingId(String buildingId) {
		this.buildingId = buildingId;
		return this;
	}
	public String getDescription() {
		return description;
	}

	public BuildingContent setDescription(String description) {
		this.description = description;
		return this;
	}

	public HashMap<String, HomeContent> getHomeContentMap() {
		return homeContentMap;
	}

	public BuildingContent setHomeContentMap(HashMap<String, HomeContent> homeContentMap) {
		this.homeContentMap = homeContentMap;
		return this;
	}

	public BuildingContent addHomeContent(HomeContent homeContent){
		this.homeContentMap.put(homeContent.getHomeID(), homeContent);
		return this;
	}
	
	public String getAddress() {
		return address;
	}

	public BuildingContent setAddress(String address) {
		this.address = address;
		return this;
	}

	public String getCap() {
		return cap;
	}

	public BuildingContent setCap(String cap) {
		this.cap = cap;
		return this;
	}

	public String getCity() {
		return city;
	}

	public BuildingContent setCity(String city) {
		this.city = city;
		return this;
	}

	public String getNation() {
		return nation;
	}

	public BuildingContent setNation(String nation) {
		this.nation = nation;
		return this;
	}

	public String getBuildingId() {
		return buildingId;
	}
	
}