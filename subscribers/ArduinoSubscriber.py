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
subscriberName = "ArduinoSubscriber"

actuators = "192.168.1.2_2;192.168.1.2_3;192.168.1.3_7;192.168.1.3_8;192.168.1.4_7;192.168.1.4_8;"
switchon = "http://%s:8082/set&relay=%s&value=1"
switchoff = "http://%s:8082/set&relay=%s&value=0"
configuration = "http://%s:8082/getConfiguration"

#logLevel = logging.INFO
logLevel = logging.DEBUG


class ArduinoSubscriber(AbstractSubscriber):
	def __init__ (self):
		super(ArduinoSubscriber, self).__init__(subscriberName, logLevel)
		self.mqttc = MyMQTTClass(subscriberName, self.logger, self)
		self.mqttc.connect(brokerUri, brokerPort)
		self.mqttc.subscribeEvent(actuators.lower(), EventTopics.getActuatorAction())		

		self.loop()

	def notifyJsonEvent(self, topic, jsonEventString):
		self.logger.debug ("received topic: \"%s\" with msg: \"%s\"" % (topic, jsonEventString))

		try: 
			data = json.loads(jsonEventString)
			ip, pin = ''.join(data["device"].split()).split('_')

			if ( data["value"].lower() == ActuationCommands.getSwitchOn().lower() ):
				action = switchon % (ip, pin)
				self.logger.debug ("Calling %s" % action)
			elif ( data["value"].lower() == ActuationCommands.getSwitchOff().lower() ):
				action = switchoff % (ip, pin)
				self.logger.debug ("Calling %s" % action)
			elif ( data["value"].lower() == ActuationCommands.getConfiguration().lower() ):
				action = configuration % (ip, pin)
				self.logger.debug ("Calling %s" % action)
			else:
				self.logger.error("Command %s unknown" % data["value"])

			resp = self.invokeWebService(action)
			self.logger.debug("Web Service reponse %s: " % resp)
		except Exception, e:
			self.logger.error("Error on ArduinoSubscriber.notifyJsonEvent() %s: " % e)


if __name__ == "__main__":
	ps = ArduinoSubscriber()
		