#!/usr/bin/env python


#About configuration sections

def getRuleSettings():
	return "rule_settings"

def getGeneralSettings():
	return "general_settings"

def getMondayOff():
	return "Monday_off"

def getTuesdayOff():
	return "Tuesday_off"

def getWednesdayOff():
	return "Wednesday_off"

def getThursdayOff():
	return "Thursday_off"

def getFridayOff():
	return "Friday_off"

def getSaturdayOff():
	return "Saturday_off"

def getSundayOff():
	return "Sunday_off"

def getRuleSettingsKeywords():
	return [getIsLooked()]

def getGeneralSettingsKeywords():
	return [getRuleSID(), getRuleDescription(), getRoomID(), getIsRuleEnabled(), getPresenceTimer(), getAbsenceTimer(), getFullUserList(), getFullSensorList(), getFullActuatorList(), getFullButtonList(),
	getMessageBroker(), getExternalMessageBroker()]

def getWeekdaysSection():
	return [getMondayOff(), getTuesdayOff(), getWednesdayOff(), getThursdayOff(), getFridayOff(), getSaturdayOff(), getSundayOff()]



# General settings to be saved

def getRuleSID():
	return "rulesid"

def getRuleDescription():
	return "ruledescription"

def getRoomID():
	return "roomid"

def getIsRuleEnabled():
	return "isruleenabled"

def getPresenceTimer():
	return "presencetimer"

def getIsLooked():
	return "isLooked"

def getAbsenceTimer():
	return "absencetimer"

def getFullUserList():
#	return "fulluserlist"
	return "userlist"

def getFullSensorList():
#	return "fullsensorlist"
	return "sensorlist"

def getFullActuatorList():
#	return "fullactuatorlist"
	return "actuatorlist"

def getFullButtonList():
#	return "fullbuttonlist"
	return "buttonlist"

def getMessageBroker():
	return "messagebroker"

def getExternalMessageBroker():
	return "externalmessagebroker"



# General setting to do not save
def getIsDelayTimerOn():
	return "isdelaytimeron"

def getMotion():
	return "motion"

def getPresence():
	return "presence"

def getStartingDate():
	return "startingdate"

def getLastExecutionDate():
	return "lastexecutiondate"


# weekdays settings

def getStartTime():
	return "start_time"

def getEndTime():
	return "end_time"

def getWeekdayOffState():
	return "weekday_off_state"

def getWeekdayOnState():
	return "weekday_on_state"
