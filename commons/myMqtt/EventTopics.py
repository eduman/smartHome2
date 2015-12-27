#!/usr/bin/env python

#About measurements
def getSensorMeasurementEvent():
	return "MEASUREMENT/SENSOR"

#About actions
def getActuatorAction():
	return "ACTION/ACTUATOR"

def getLookAction():
	return "ACTION/LOOK"

def getSwitchOffAll():
	return "ACTION/ACTUATOR/SWITCHOFALL"

def getSystemNotificationMessage():
	return "SYSTEM/NOTIFICATION"

def getRuleEnabler():
	return "RULE_ENABLER"
	
# About Behaviours
def getBehaviourProximity():
	return  "BEHAVIOURS/PROXIMITY"

def getBehaviourMotion():
	return  "BEHAVIOURS/MOTION"

def getBehaviourButtonPushed():
	return  "BEHAVIOURS/BUTTONPUSHED"

