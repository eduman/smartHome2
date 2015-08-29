#!/usr/bin/env python

from AbstractActionRule import AbstractActionRule
import time
import ConfigurationConstants
#import threading
from Context import Context
import logging
import datetime
#from DefaultTimerRule import DefaultTimerRule
import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import EventTopics
from smartHomeDevice import ActuationCommands
from utitlityLib import Utilities


class TimeShiftActionRule(AbstractActionRule):

	def __init__(self, context, logger, mqttServiceProvider):
		super(TimeShiftActionRule, self).__init__(context, logger, mqttServiceProvider)
		self.isOffState = False
		self.isOnState = False


	def process(self):
		self.logger.debug("Processing TimeShiftActionRule...")
		isRuleEnabled = True
		try:
			isRuleEnabled = Utilities.to_bool(self.context.getProperty(ConfigurationConstants.getIsRuleEnabled()))
		except ValueError, e:
			self.logger.error("Error on TimeShiftActionRule.process(): %s. Setting True as default value for isRuleEnabled" % (e))
		
		try:
			self.isOffState = Utilities.to_bool(self.context.getProperty(ConfigurationConstants.getWeekdayOffState()))
			self.isOnState = Utilities.to_bool(self.context.getProperty(ConfigurationConstants.getWeekdayOnState()))
		except ValueError, e:
			self.logger.error("Error on TimeShiftActionRule.process(): %s. Setting False as default value for isOffState and isOnState" % (e))
			self.isOffState = False
			self.isOnState = False


		if isRuleEnabled:
			weekday = datetime.datetime.today().weekday()

			if (weekday == 0):
				#Monday
				self.action(ConfigurationConstants.getMondayOff())

			elif (weekday == 1):
				#Tuesday
				self.action(ConfigurationConstants.getTuesdayOff())

			elif (weekday == 2):
				#Wednesday
				self.action(ConfigurationConstants.getWednesdayOff())

			elif (weekday == 3):
				#Thursday
				self.action(ConfigurationConstants.getThursdayOff())

			elif (weekday == 4):
				#Friday
				self.action(ConfigurationConstants.getFridayOff())

			elif (weekday == 5):
				#Saturday
				self.action(ConfigurationConstants.getSaturdayOff())

			elif (weekday == 6):
				#Sunday
				self.action(ConfigurationConstants.getSundayOff())

			else:
				self.logger.error("Error on TimeShiftActionRule.process(): Unknown weekday.")


			self.context.updateProperty(ConfigurationConstants.getWeekdayOffState(), self.isOffState)
			self.context.updateProperty(ConfigurationConstants.getWeekdayOnState(), self.isOnState)

	def stop(self):
		#TODO
		pass

	def action(self, day_off):
		try: 
			startStr = self.context.getProperty(day_off+"_"+ConfigurationConstants.getStartTime())
			endStr = self.context.getProperty(day_off+"_"+ConfigurationConstants.getEndTime())

			if startStr and endStr:
				startH, startM = startStr.split(":")
				endH, endM = endStr.split(":")
				now =  datetime.datetime.now()
				start = now.replace(hour=int(startH), minute=int(startM), second=0, microsecond=0)
				end = now.replace(hour=int(endH), minute=int(endM), second=0, microsecond=0)
				
				if now >= start and now < end and self.isOffState == False:
					self.logger.debug("TimeShiftActionRule.action(): OFF Status, switching off the appliances")
					self.isOffState = True
					self.isOnState = False
					self.sendCommands(ActuationCommands.getSwitchOff())
					
				#elif now < start and now >= end and self.isOnState == False:
				elif now >= end and self.isOnState == False:
					self.logger.debug("TimeShiftActionRule.action(): ON Status, switching on the appliances")
					self.isOffState = False
					self.isOnState = True
					self.sendCommands(ActuationCommands.getSwitchOn())

				elif now < start and self.isOnState == False:
					self.logger.debug("TimeShiftActionRule.action(): ON Status, switching on the appliances")
					self.isOffState = False
					self.isOnState = True
					self.sendCommands(ActuationCommands.getSwitchOn())
			
			elif self.isOnState == False:
				# by default the device are on
				self.logger.debug("TimeShiftActionRule.action(): Default ON Status, switching on the appliances")
				self.isOffState = False
				self.isOnState = True
				self.sendCommands(ActuationCommands.getSwitchOn())

		except Exception, e:
			self.logger.error("Error on TimeShiftActionRule.action(): %s." % (e))

	def sendCommands(self, action):
		fullActuatorList = self.context.getProperty(ConfigurationConstants.getFullActuatorList())
		if fullActuatorList:
			tokens =  ''.join(fullActuatorList.split()).split(';')
			for tok in tokens:
				if tok:
					eventTopic = self.makeActionEvent(EventTopics.getActuatorAction(), tok, action)
					self.publishEvent(eventTopic, tok, action, action)
					try:
						time.sleep(0.01)
					except Exception, e:
						self.logger.error("Error on TimeShiftActionRule.sendCommands(): %s" % (e))


if __name__ == "__main__":
	context = Context(3)
	logger = logging.getLogger('RestoreStatusRule')
	logger.setLevel(logging.DEBUG)
	hdlr = logging.FileHandler('log/rule.log')
	formatter = logging.Formatter("%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
	hdlr.setFormatter(formatter)
	logger.addHandler(hdlr)
	consoleHandler = logging.StreamHandler()
	consoleHandler.setFormatter(formatter)
	logger.addHandler(consoleHandler)
	rule = DefaultTimerRule(context, logger, "conf/agents/rule.conf")
	rule.process()
	rule2 = DelayTimerActionRule(context, logger, None)
	rule2.process()
	time.sleep(10.0)
	context.updateProperty(ConfigurationConstants.getPresence(), str(True))
	rule2.process()
	time.sleep(10.0)
	context.updateProperty("sensor1", str(True))
	rule2.process()
