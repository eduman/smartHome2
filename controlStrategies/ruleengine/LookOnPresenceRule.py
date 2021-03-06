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




class LookOnPresenceRule(AbstractActionRule):

	def __init__(self, context, logger, mqttServiceProvider):
		super(LookOnPresenceRule, self).__init__(context, logger, mqttServiceProvider)


	def process(self):
		self.logger.debug("Processing LookOnPresenceRule...")
		isRuleEnabled = True
		presence = False

		try:
			isRuleEnabled = Utilities.to_bool(self.context.getProperty(ConfigurationConstants.getIsRuleEnabled()))
		except ValueError, e:
			self.logger.error("Error on LookOnPresenceRule.process(): %s. Setting True as default value for isRuleEnabled" % (e))
		

		if isRuleEnabled:

			fullUserList = self.context.getProperty(ConfigurationConstants.getFullUserList())
			if fullUserList:
				users = ''.join(fullUserList.split()).split(';')
				for user in users:
					if user:
						try:
							presence |= Utilities.to_bool(self.context.getProperty(user))
						except Exception, e:
							self.logger.error("Error on LookOnPresenceRule.process(): Malformed boolean exception for user = %s. %s." % (user, e))

#			try:
#				alldevices = Utilities.to_bool(self.context.getProperty("alldevices"))
#				if (alldevices == True):
#					presence = alldevices
#			except Exception, e:
#				self.logger.error("Error on LookOnPresenceRule.process(): Malformed boolean exception for alldevices = %s. %s." % (alldevices, e))


			if (presence):
				self.publishUnlook()
			else:
				self.publishLook()



	def stop(self):
		pass

	def publishLook(self):
		eventTopic = self.makeActionEvent(EventTopics.getLookAction(), "alldevices", ActuationCommands.getLook())
		self.publishEvent(eventTopic, "alldevices", ActuationCommands.getLook(), True)

	def publishUnlook(self):
		eventTopic = self.makeActionEvent(EventTopics.getLookAction(), "alldevices", ActuationCommands.getLook())
		self.publishEvent(eventTopic, "alldevices", ActuationCommands.getLook(), False)
	


