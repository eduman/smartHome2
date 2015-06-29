#!/usr/bin/env python

import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)

from abstractSubscriber.AbstractSubscriber import AbstractSubscriber
from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass
import logging
import os
import signal
import sys
import json
from smartHomeDevice import ActuationCommands
#import ConfigParser
#import inspect


brokerUri = "192.168.1.5"
#brokerUri = "localhost"
brokerPort = "1883"
subscriberName = "PlugwiseSubscriber"
#actuators = "000d6f0000998ab5;000d6f0000af5093;000d6f0000d362b0;000d6f0000b1d4ec;000d6f0000af5096;000d6f0000af4e16;000d6f0000af5094"

homeWSUri = "http://192.168.1.5:8080/rest/home/configuration"
connectorURI = "http://192.168.1.5:8080"
#connectorURI = "http://localhost:8080"
configuration = connectorURI + "/rest/plugwise/%s/configuration"
switchon = connectorURI + "/rest/plugwise/%s/on"
switchoff = connectorURI + "/rest/plugwise/%s/off"

#logLevel = logging.INFO
logLevel = logging.DEBUG

class PlugwiseSubscriber (AbstractSubscriber):
	def __init__ (self):
		super(PlugwiseSubscriber, self).__init__(subscriberName, logLevel)

#		for sig in (signal.SIGABRT, signal.SIGILL, signal.SIGINT, signal.SIGSEGV, signal.SIGTERM):
#			signal.signal(sig, self.signal_handler)

		resp, isOk = self.invokeWebService(homeWSUri)
		if isOk:
			myhome = json.loads(resp)
			actuators = ""
			for i, room in enumerate(myhome['rooms']):				
				for i, device in enumerate(room['devices']):
					if device['type'].lower() == "plugwise":
						actuators += str (device['deviceID'].lower()) + ";"
			actuators = actuators[:-1]
			self.mqttc = MyMQTTClass(subscriberName, self.logger, self)
			self.mqttc.connect(brokerUri, brokerPort)
			self.mqttc.subscribeEvent(actuators.lower(), EventTopics.getActuatorAction())
	
		self.loop()

	def notifyJsonEvent(self, topic, jsonEventString):
		self.logger.debug ("received topic: \"%s\" with msg: \"%s\"" % (topic, jsonEventString))

		try:
			data = json.loads(jsonEventString)

			if ( data["value"].lower() == ActuationCommands.getSwitchOn().lower() ):
				action = switchon % data["device"]
				self.logger.debug ("Calling %s" % action)
			elif ( data["value"].lower() == ActuationCommands.getSwitchOff().lower() ):
				action = switchoff % data["device"]
				self.logger.debug ("Calling %s" % action)
			elif ( data["value"].lower() == ActuationCommands.getConfiguration().lower() ):
				action = configuration % data["device"]
				self.logger.debug ("Calling %s" % action)
			else:
				self.logger.error("Command %s unknown" % data["value"])

			resp = self.invokeWebService(action)
			self.logger.debug("Web Service reponse %s: " % resp)
		except Exception, e:
			self.logger.error("Error on PlugwiseSubscriber.notifyJsonEvent() %s: " % e)

if __name__ == "__main__":
	ps = PlugwiseSubscriber()