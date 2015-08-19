package it.eduman.smartHome.webServices;

public class QueryContent {

	private String username;
	private long messageLongID;
	private String deviceID;
	private String actuator;
	private String command;
	private String value;
	private String genericStringID;
	private String measure;
	private boolean areMeasurementsIncluded;
	private boolean areHomeIncluded;
	private boolean isApproaching;
	
	public String getUsername() {
		return username;
	}
	public QueryContent setUsername(String username) {
		this.username = username;
		return this;
	}
	public long getMessageLongID() {
		return messageLongID;
	}
	public QueryContent setMessageLongID(long messageID) {
		this.messageLongID = messageID;
		return this;
	}
	public String getDeviceID() {
		return deviceID;
	}
	public QueryContent setDeviceID(String deviceID) {
		this.deviceID = deviceID;
		return this;
	}
	public String getActuator() {
		return actuator;
	}
	public QueryContent setActuator(String actuator) {
		this.actuator = actuator;
		return this;
	}
	public String getCommand() {
		return command;
	}
	public QueryContent setCommand(String command) {
		this.command = command;
		return this;
	}
	public String getValue() {
		return value;
	}
	public QueryContent setValue(String value) {
		this.value = value;
		return this;
	}
	public String getGenericStringID() {
		return genericStringID;
	}
	public QueryContent setGenericStringID(String genericStringID) {
		this.genericStringID = genericStringID;
		return this;
	}
	public String getMeasure() {
		return measure;
	}
	public QueryContent setMeasure(String measure) {
		this.measure = measure;
		return this;
	}
	public boolean isAreMeasurementsIncluded() {
		return areMeasurementsIncluded;
	}
	public QueryContent setAreMeasurementsIncluded(boolean areMeasurementsIncluded) {
		this.areMeasurementsIncluded = areMeasurementsIncluded;
		return this;
	}
	public boolean isAreHomeIncluded() {
		return areHomeIncluded;
	}
	public QueryContent setAreHomeIncluded(boolean areHomeIncluded) {
		this.areHomeIncluded = areHomeIncluded;
		return this;
	}
	public boolean isApproaching() {
		return isApproaching;
	}
	public QueryContent setApproaching(boolean isApproaching) {
		this.isApproaching = isApproaching;
		return this;
	}
	
	
	
	
}
