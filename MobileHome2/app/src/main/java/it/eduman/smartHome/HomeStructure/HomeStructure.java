package it.eduman.smartHome.HomeStructure;


import java.util.List;

public class HomeStructure {
    private String homeID;
    private String description;
    private String floor;
    private String landlord;
    private String buildingID;
    private UserPresenceManager userPresenceManager;
    private String switchOffAllDevicesAgent;
    private String dashboard;

    private List<ThingspeakChannel> thingspeakChannels;

    private MessageBroker homeMessageBroker;
    private MessageBroker externalMessageBroker;

    private List <Room>rooms;

    private List<Rule> rules;

    public String getHomeID() {
        return homeID;
    }

    public void setHomeID(String homeID) {
        this.homeID = homeID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getLandlord() {
        return landlord;
    }

    public void setLandlord(String landlord) {
        this.landlord = landlord;
    }

    public String getBuildingID() {
        return buildingID;
    }

    public void setBuildingID(String buildingID) {
        this.buildingID = buildingID;
    }

    public UserPresenceManager getUserPresenceManager() {
        return userPresenceManager;
    }

    public void setUserPresenceManager(UserPresenceManager userPresenceManager) {
        this.userPresenceManager = userPresenceManager;
    }

    public String getDashboard() {
        return dashboard;
    }

    public void setDashboard(String dashboard) {
        this.dashboard = dashboard;
    }

    public MessageBroker getHomeMessageBroker() {
        return homeMessageBroker;
    }

    public void setHomeMessageBroker(MessageBroker homeMessageBroker) {
        this.homeMessageBroker = homeMessageBroker;
    }

    public MessageBroker getExternalMessageBroker() {
        return externalMessageBroker;
    }

    public void setExternalMessageBroker(MessageBroker externalMessageBroker) {
        this.externalMessageBroker = externalMessageBroker;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public String getSwitchOffAllDevicesAgent() {
        return switchOffAllDevicesAgent;
    }

    public void setSwitchOffAllDevicesAgent(String switchOffAllDevicesAgent) {
        this.switchOffAllDevicesAgent = switchOffAllDevicesAgent;
    }

    public List<ThingspeakChannel> getThingspeakChannels() {
        return thingspeakChannels;
    }

    public void setThingspeakChannels(List<ThingspeakChannel> thingspeakChannels) {
        this.thingspeakChannels = thingspeakChannels;
    }
}
