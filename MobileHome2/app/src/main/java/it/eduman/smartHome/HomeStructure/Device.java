package it.eduman.smartHome.HomeStructure;


import java.util.List;

public class Device {
    private String deviceID;
    private String description;
    private boolean isActive;
    private String type;
    private List<DeviceProtocol> protocol;
    private List<ThingspeakChannel> thingspeakChannels;



    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<DeviceProtocol> getProtocol() {
        return protocol;
    }

    public void setProtocol(List<DeviceProtocol> protocol) {
        this.protocol = protocol;
    }

    public List<ThingspeakChannel> getThingspeakChannels() {
        return thingspeakChannels;
    }

    public void setThingspeakChannels(List<ThingspeakChannel> thingspeakChannels) {
        this.thingspeakChannels = thingspeakChannels;
    }
}
