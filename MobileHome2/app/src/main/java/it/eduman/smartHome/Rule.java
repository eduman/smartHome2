package it.eduman.smartHome;


import java.util.List;

public class Rule {
    private String ruleSID;
    private String ruleDescription;
    private boolean isRuleEnabled;
    private String roomID;
    private int presencetimer;
    private int absencetimer;
    private List<Actuator> actuatorList;
    private List<String> userList;
    private List<String> sensorList;
    private List<String> buttonList;
    private List<TimeShift> timeShifts;

    public String getRuleSID() {
        return ruleSID;
    }

    public void setRuleSID(String ruleSID) {
        this.ruleSID = ruleSID;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

    public boolean isRuleEnabled() {
        return isRuleEnabled;
    }

    public void setIsRuleEnabled(boolean isRuleEnabled) {
        this.isRuleEnabled = isRuleEnabled;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public int getPresencetimer() {
        return presencetimer;
    }

    public void setPresencetimer(int presencetimer) {
        this.presencetimer = presencetimer;
    }

    public int getAbsencetimer() {
        return absencetimer;
    }

    public void setAbsencetimer(int absencetimer) {
        this.absencetimer = absencetimer;
    }

    public List<Actuator> getActuatorList() {
        return actuatorList;
    }

    public void setActuatorList(List<Actuator> actuatorList) {
        this.actuatorList = actuatorList;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }

    public List<String> getSensorList() {
        return sensorList;
    }

    public void setSensorList(List<String> sensorList) {
        this.sensorList = sensorList;
    }

    public List<String> getButtonList() {
        return buttonList;
    }

    public void setButtonList(List<String> buttonList) {
        this.buttonList = buttonList;
    }

    public List<TimeShift> getTimeShifts() {
        return timeShifts;
    }

    public void setTimeShifts(List<TimeShift> timeShifts) {
        this.timeShifts = timeShifts;
    }
}
