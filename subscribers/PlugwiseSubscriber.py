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




subscriberName = "PlugwiseSubscriber"
deviceType = "plugwise"

#homeWSUri = "http://localhost:8080/rest/home/configuration"
homeWSUri = "http://192.168.1.5:8080/rest/home/configuration"

connectorURI = "http://192.168.1.5:8080"
configuration = connectorURI + "/rest/plugwise/%s/configuration"
switchon = connectorURI + "/rest/plugwise/%s/on"
switchoff = connectorURI + "/rest/plugwise/%s/off"

#logLevel = logging.INFO
logLevel = logging.DEBUG

class PlugwiseSubscriber (AbstractSubscriber):
	def __init__ (self):
		super(PlugwiseSubscriber, self).__init__(subscriberName, homeWSUri, deviceType, logLevel)


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
	ps.start()