package it.eduman.smartHome.deprecated.device;

import it.eduman.smartHome.IoTDevice.ActuationCommands;

public class HardwarePinStatusContent {

	private int pin;
	private String configuredAs;
	private String type;
	private String status;
	private String actuationCommand = ActuationCommands.CommonCommands.UnknownCommand.toString();
	private String deviceID;
	
	public HardwarePinStatusContent (int pin, String configuredAs){
		this.pin = pin;
		this.configuredAs = configuredAs;
	}
	
	public int getPin() {
		return pin;
	}


	public HardwarePinStatusContent setPin(int pin) {
		this.pin = pin;
		return this;
	}


	public String getConfiguredAs() {
		return configuredAs;
	}

	public HardwarePinStatusContent setConfiguredAs(String configuredAs) {
		this.configuredAs = configuredAs;
		return this;
	}


	public String getType() {
		return type;
	}

	public HardwarePinStatusContent setType(String type) {
		this.type = type;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public HardwarePinStatusContent setStatus(String status) {
		this.status = status;
		return this;
	}
	
	public HardwarePinStatusContent setActuationCommand (String nextCommand){
		this.actuationCommand = nextCommand;
		return this;
	}
	
	public String getActuationCommand(){
		return this.actuationCommand;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public HardwarePinStatusContent setDeviceID(String deviceID) {
		this.deviceID = deviceID;
		return this;
	}
	
}
