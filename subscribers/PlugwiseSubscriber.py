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



subscriberName = "PlugwiseSubscriber"
deviceType = "plugwise"


logLevel = logging.INFO
#logLevel = logging.DEBUG

class PlugwiseSubscriber (AbstractSubscriber):
	def __init__ (self):
		super(PlugwiseSubscriber, self).__init__(subscriberName, deviceType, logLevel)

		try:
			self.endpoint = CommonConfigurator.getPlugwiseEndPointValue(self.commonConfigPath)
			self.configuration = self.endpoint + "/rest/plugwise/%s/configuration"
			self.switchon = self.endpoint + "/rest/plugwise/%s/on"
			self.switchoff = self.endpoint + "/rest/plugwise/%s/off"
		
		except Exception, e:
			self.logger.error('Unable to start %s due to: %s' % (self.subscriberName, e))
			self.stop()


	def notifyJsonEvent(self, topic, jsonEventString):
		self.logger.debug ("received topic: \"%s\" with msg: \"%s\"" % (topic, jsonEventString))

		try:
			data = json.loads(jsonEventString)

			if ( data["value"].lower() == ActuationCommands.getSwitchOn().lower() ):
				action = self.switchon % data["device"]
				self.logger.debug ("Calling %s" % action)
			elif ( data["value"].lower() == ActuationCommands.getSwitchOff().lower() ):
				action = self.switchoff % data["device"]
				self.logger.debug ("Calling %s" % action)
			elif ( data["value"].lower() == ActuationCommands.getConfiguration().lower() ):
				action = self.configuration % data["device"]
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