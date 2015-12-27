#!/usr/bin/env python

from AbstractActionRule import AbstractActionRule
import time
import ConfigurationConstants
import threading
from Context import Context
import logging
import datetime

import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import EventTopics
from smartHomeDevice import ActuationCommands
from utitlityLib import Utilities

from subprocess import call
import StringIO
import time
from time import strftime
import os




class SwitchOffAllActionRule(AbstractActionRule):

	def __init__(self, context, logger, mqttServiceProvider):
		super(SwitchOffAllActionRule, self).__init__(context, logger, mqttServiceProvider)


	def process(self):
		self.logger.debug("Processing SwitchOffAllActionRule...")
		isRuleEnabled = True
		presence = False

		try:
			isRuleEnabled = Utilities.to_bool(self.context.getProperty(ConfigurationConstants.getIsRuleEnabled()))
		except ValueError, e:
			self.logger.error("Error on SwitchOffAllActionRule.process(): %s. Setting True as default value for isRuleEnabled" % (e))
		

		isTobeOff =self.context.getProperty(ConfigurationConstants.getSwitchOffAll())

		if isRuleEnabled and isTobeOff == "True":

			fullActuatorList = self.context.getProperty(ConfigurationConstants.getFullActuatorList())
			if fullActuatorList:
				devices = ''.join(fullActuatorList.split()).split(';')
				for dev in devices:
					if dev:
						try:
							eventTopic = self.makeActionEvent(EventTopics.getActuatorAction(), dev, ActuationCommands.getSwitchOff())
							self.publishEvent(eventTopic, dev, ActuationCommands.getSwitchOff(), ActuationCommands.getSwitchOff())
							self.context.updateProperty(ConfigurationConstants.getSwitchOffAll(), "False")
						except Exception, e:
							self.logger.error("Error on SwitchOffAllActionRule.process(): %s" % (e))


	def stop(self):
		pass




