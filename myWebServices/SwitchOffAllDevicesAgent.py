#!/usr/bin/python

import cherrypy
import logging
import os
import datetime
import time
import os, sys
import shutil
import json

import abstractAgent.AbstractAgent as AbstractAgent
from abstractAgent.AbstractAgent import AbstractAgentClass

lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import MQTTPayload 
from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass  
from mySSLUtil import MySSLUtil


DEFAULT_BROKER_URI = "localhost"
DEFAULT_BROKER_PORT = "1883"

httpPort = 8086
#httpPort = 443
#logLevel = logging.DEBUG
logLevel = logging.INFO

class SwitchOffAllDevicesAgent(AbstractAgentClass):
	exposed = True

	def __init__(self, serviceName, logLevel):
		super(SwitchOffAllDevicesAgent, self).__init__(serviceName, logLevel)
		self.myhome = self.retriveHomeSettings()
		self.brokerUri = self.myhome["homeMessageBroker"]["address"]
		self.brokerPort = self.myhome["homeMessageBroker"]["port"]
		

	def getMountPoint(self):
		return '/rest/switchoffall'

	def start (self):
		self.mqtt = MyMQTTClass(self.serviceName, self.logger, self)
		self.mqtt.connect(self.brokerUri, self.brokerPort)

		self.logger.info("Started")



	def stop(self):
		if (hasattr (self, "mqtt")):
			try:
				self.mqtt.disconnect()
			except Exception, e:
				self.logger.error("Error on stop(): %s" % (e))

		self.logger.info("Ended")


	def GET(self, *ids, **params):
		result = ""
		if len(ids) > 0:
			param_0 = str(ids[0]).lower()
			if param_0 == "actuate":					
				timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
				payload = (MQTTPayload.getActuationPayload() %  (EventTopics.getSwitchOffAll(), "True", "switchoffall", "no_device", str(timestamp)))		
				self.mqtt.syncPublish(EventTopics.getSwitchOffAll(), payload, 2)
				result += payload

		else:
			self.logger.error("Command not found")
			raise cherrypy.HTTPError("404 Not found", "command not found")

	
		return result	

		
	def POST(self, *ids, **params):
		self.logger.error("Subclasses must override POST(self, *ids)!")
		raise NotImplementedError('subclasses must override POST(self, *ids)!')
		
	def PUT(self, *ids, **params):
		self.logger.error("Subclasses must override PUT(self, *ids)!")
		raise NotImplementedError('subclasses must override PUT(self, *ids)!')
		
	def DELETE(self, *ids, **params):
		self.logger.error("Subclasses must override DELETE(self, *ids)!")
		raise NotImplementedError('subclasses must override DELETE(self, *ids)!')


if __name__ == "__main__":
	switchOff = SwitchOffAllDevicesAgent("SwitchOffAllDevicesAgent", logLevel)
	AbstractAgent.startCherrypy(httpPort, switchOff)



