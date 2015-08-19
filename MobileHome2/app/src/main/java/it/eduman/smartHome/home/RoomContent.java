package it.eduman.smartHome.home;

import it.eduman.smartHome.device.DeviceContent;

import java.util.HashMap;

public class RoomContent {
	private String roomID;
	private String description = "Unknown";
	private HashMap<String, DeviceContent> devicesMap = new HashMap<String, DeviceContent>();
	private String homeID;
	
	public RoomContent (){}
	
	public RoomContent (String roomID){
		this.roomID = roomID;
	}

	public String getRoomID() {
		return roomID;
	}

	public RoomContent setRoomID(String roomID) {
		this.roomID = roomID;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public RoomContent setDescription(String description) {
		this.description = description;
		return this;
	}

	public HashMap<String, DeviceContent> getDevicesMap() {
		return devicesMap;
	}

	public RoomContent setDevicesMap(HashMap<String, DeviceContent> devicesMap) {
		this.devicesMap = devicesMap;
		return this;
	}
	
	public RoomContent addDeviceContent (DeviceContent deviceContent) {
		this.devicesMap.put(deviceContent.getDeviceID(), deviceContent);
		return this;
	}

	public String getHomeID() {
		return homeID;
	}

	public void setHomeID(String homeID) {
		this.homeID = homeID;
	}
	
}
