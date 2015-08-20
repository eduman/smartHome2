package it.eduman.smartHome.deprecated.device;
import java.util.ArrayList;
import java.util.List;


public class DeviceContent{
	
	protected String deviceID;
	protected String description = "Unknown";
	protected String linkSmartDescription = "Unknown";
	protected boolean isActive = true;
	protected boolean isConfigured = true;
	protected String roomID;
	
	
	private List<HardwarePinStatusContent> hardwarePinStatusList = new ArrayList<HardwarePinStatusContent>();
	private List<MeasurementContent> measurementList = new ArrayList<MeasurementContent>();
	
	public DeviceContent(String deviceID, String linkSmartDescription){
		this.deviceID = deviceID;
		this.linkSmartDescription = linkSmartDescription;
		this.isActive = true;
		this.isConfigured = true;
	}
	
	public String getDeviceID() {
		return deviceID;
	}

	public DeviceContent setDeviceID(String deviceID) {
		this.deviceID = deviceID;
		return this;
	}

	public String getDescription(){
		return this.description;
	}
	
	public DeviceContent setDescription(String description) {
		this.description = description;
		return this;
	}
	
	public String getLinkSmartDescription() {
		return this.linkSmartDescription;
	}
	
	public DeviceContent setLinkSmartDescription (String linkSmartDescription){
		this.linkSmartDescription = linkSmartDescription;
		return this;
	}
	
	public List<HardwarePinStatusContent> getHardwarePinStatusList() {
		return hardwarePinStatusList;
	}

	public DeviceContent setHardwarePinStatusList(List<HardwarePinStatusContent> hardwarePinStatusList) {
		this.hardwarePinStatusList = hardwarePinStatusList;
		return this;
	}
	
	public boolean isActive() {
		return isActive;
	}

	public DeviceContent setActive(boolean isActive) {
		this.isActive = isActive;
		return this;
	}

	public boolean isConfigured() {
		return isConfigured;
	}

	public DeviceContent setConfigured(boolean isConfigured) {
		this.isConfigured = isConfigured;
		return this;
	}

	public List<MeasurementContent> getMeasurementList() {
		return measurementList;
	}

	public DeviceContent setMeasurementList(List<MeasurementContent> measurementList) {
		this.measurementList = measurementList;
		return this;
	}


	

	public String getRoomID() {
		return roomID;
	}

	public DeviceContent setRoomID(String roomID) {
		this.roomID = roomID;
		return this;
	}	
	
	
	public static String makeLinksmartDescription (String serviceName, String moteID){
		return serviceName + ":" + moteID;
	}

}