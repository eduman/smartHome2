package it.eduman.smartHome.HomeStructure;


import java.util.List;

public class HomeStructure {
    private String homeID;
    private String description;
    private String floor;
    private String landlord;
    private String buildingID;
    private String userPresenceManager;
    private String ruleUpdater;
    private String thingspeakChannel;

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

    public String getUserPresenceManager() {
        return userPresenceManager;
    }

    public void setUserPresenceManager(String userPresenceManager) {
        this.userPresenceManager = userPresenceManager;
    }

    public String getRuleUpdater() {
        return ruleUpdater;
    }

    public void setRuleUpdater(String ruleUpdater) {
        this.ruleUpdater = ruleUpdater;
    }

    public String getThingspeakChannel() {
        return thingspeakChannel;
    }

    public void setThingspeakChannel(String thingspeakChannel) {
        this.thingspeakChannel = thingspeakChannel;
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
}
