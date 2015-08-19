package it.eduman.smartHome.home;

import java.util.HashMap;

public class HomeContent {
	private String homeID;
	private String description = "Unknown";
	private HashMap<String, RoomContent> roomsMap = new HashMap<String, RoomContent>();
	private String floor = "Unknown Floor";
	private String landlord = "Unknown Landlord";
	private String buildingID;

	
	public HomeContent (){}
	
	public HomeContent (String homeID){
		this.homeID = homeID;
	}

	public String getHomeID() {
		return homeID;
	}

	public HomeContent setHomeID(String homeID) {
		this.homeID = homeID;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public HomeContent setDescription(String description) {
		this.description = description;
		return this;
	}

	public HashMap<String, RoomContent> getRoomsMap() {
		return roomsMap;
	}

	public HomeContent setRoomsMap(HashMap<String, RoomContent> roomsMap) {
		this.roomsMap = roomsMap;
		return this;
	}
	
	public HomeContent addRoom(RoomContent roomContent){
		this.roomsMap.put(roomContent.getRoomID(), roomContent);
		return this;
	}

	public String getFloor() {
		return floor;
	}

	public HomeContent setFloor(String floor) {
		this.floor = floor;
		return this;
	}

	public String getLandlord() {
		return landlord;
	}

	public HomeContent setLandlord(String landlord) {
		this.landlord = landlord;
		return this;
	}

	public String getBuildingID() {
		return buildingID;
	}

	public HomeContent setBuildingID(String buildingID) {
		this.buildingID = buildingID;
		return this;
	}
	
	

}
