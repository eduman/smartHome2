#!/usr/bin/env python

import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)

from abstractSubscriber.AbstractSubscriber import AbstractSubscriber
from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass
from myConfigurator import CommonConfigurator  

import logging
import os
import signal
import sys
import json
from smartHomeDevice import ActuationCommands



subscriberName = "ArduinoSubscriber"
deviceType = "arduino"

switchon = "http://%s:8082/set&relay=%s&value=1"
switchoff = "http://%s:8082/set&relay=%s&value=0"
configuration = "http://%s:8082/getConfiguration"

logLevel = logging.INFO
#logLevel = logging.DEBUG


class ArduinoSubscriber(AbstractSubscriber):
	def __init__ (self):
		super(ArduinoSubscriber, self).__init__(subscriberName, deviceType,logLevel)
		

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
	ps.start()
		