#!/usr/bin/env python

import os, sys
lib_path = os.path.abspath(os.path.join('.', 'myMqtt'))
sys.path.append(lib_path)
import MQTTPayload
import datetime
import time


class AbstractActionRule(object):

	def __init__(self, context, logger, mqttc):
		self.context = context
		self.logger = logger
		self.mqttc = mqttc

	def publishEvent(self, topic, device, event, value): 
		timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
		payload = (MQTTPayload.getActuationPayload() %  (str(topic), str(value), str(event), str(device), str(timestamp)))
		self.logger.debug ("publishing: %s" % payload) 
		self.mqttc.publish(topic, payload, 2)

	def makeActionEvent(self, controlEventTopic, deviceId, function):
		return controlEventTopic + "/" + deviceId + "/" + function


	def getEventMqttClient(self):
		return self.mqttc

#	def sendErrorEmail(self, body):
		#if mailService and (not isEventManagerInErrorState ):
		#	self.logger.error("**** SEND EMAIL NOW *****");
		#	mailService.sendMail("Error in " + serviceName , body);
		#	self.isEventManagerInErrorState = True
		#TODO
#		pass
	

if __name__ == "__main__":
	pass
