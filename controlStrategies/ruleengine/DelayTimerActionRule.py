#!/usr/bin/env python

from AbstractActionRule import AbstractActionRule
import time
import ConfigurationConstants
import threading
from Context import Context
import logging
from DefaultTimerRule import DefaultTimerRule
import os, sys

lib_path = os.path.abspath(os.path.join('.', 'myMqtt'))
sys.path.append(lib_path)
import EventTopics

lib_path = os.path.abspath(os.path.join('.', 'smartHomeDevice'))
sys.path.append(lib_path)
import ActuationCommands

lib_path = os.path.abspath(os.path.join('.', 'utitlityLib'))
sys.path.append(lib_path)
import Utilities





class DelayTimerActionRule(AbstractActionRule):

	def __init__(self, context, logger, mqttServiceProvider):
		super(DelayTimerActionRule, self).__init__(context, logger, mqttServiceProvider)
		self.isDelayTimerOn = False
		self.DEFAULT_TIMER_IN_MINUTES = 10
		self.defaultTimerInMills = self.toMinutes(self.DEFAULT_TIMER_IN_MINUTES) 
		
	def run (self):
		self.logger.info("DelayTimerActionRule.run(): switching off the applicances")
		isRuleEnabled = True
		try:
			isRuleEnabled = Utilities.to_bool (self.context.getProperty(ConfigurationConstants.getIsRuleEnabled()))
		except ValueError, e:
					self.logger.error("Error on DelayTimerActionRule.run(): %s. Setting True as defalut value for isRuleEnabled" % (e))

		fullActuatorList = self.context.getProperty(ConfigurationConstants.getFullActuatorList())
		if isRuleEnabled and fullActuatorList:
			tokens =  ''.join(fullActuatorList.split()).split(';')
			for tok in tokens:
				if tok:
					eventTopic = self.makeActionEvent(EventTopics.getActuatorAction(), tok, ActuationCommands.getSwitchOff())
					self.publishEvent(eventTopic, tok, ActuationCommands.getSwitchOff(), ActuationCommands.getSwitchOff())
					try:
						time.sleep(0.01)
					except Exception, e:
						self.logger.error("Error on DelayTimerActionRule.run(): %s" % (e))

		
		self.isDelayTimerOn = False

	def process(self):
		motion = False
		presence = False
		isRuleEnabled = True
		isMotionConsidered = False
		try:
			isRuleEnabled = Utilities.to_bool(self.context.getProperty(ConfigurationConstants.getIsRuleEnabled()))
		except ValueError, e:
					self.logger.error("Error on DelayTimerActionRule.process(): %s. Setting True as default value for isRuleEnabled" % (e))

		if not isRuleEnabled:
			#disable the timers
			self.cancelTimer()
			self.isDelayTimerOn = False
			
		else:
			self.defaultTimerInMills = self.toMinutes(self.context.getProperty(ConfigurationConstants.getAbsenceTimer()), True)

			fullSensorList = self.context.getProperty(ConfigurationConstants.getFullSensorList())
			if fullSensorList:
				sensors =  ''.join(fullSensorList.split()).split(';')
				for sensor in sensors:
					if sensor:
						try:
							motion |= Utilities.to_bool(self.context.getProperty(sensor))
							isMotionConsidered = True
						except Exception, e:
							self.logger.error("Error on DelayTimerActionRule.process(): Malformed boolean exception for sensorId = %s. %s." % (sensor, e))
			else:
				isMotionConsidered = False

			
			fullUserList = self.context.getProperty(ConfigurationConstants.getFullUserList())
			if fullUserList:
				users = ''.join(fullUserList.split()).split(';')
				for user in users:
					if user:
						try:
							presence |= Utilities.to_bool(self.context.getProperty(user))
						except Exception, e:
							self.logger.error("Error on DelayTimerActionRule.process(): Malformed boolean exception for user = %s. %s." % (user, e))
			
				if presence:
					self.defaultTimerInMills = self.toMinutes(self.context.getProperty(ConfigurationConstants.getPresenceTimer()), True)

			oldPresence = Utilities.to_bool(self.context.getProperty(ConfigurationConstants.getPresence()))

			if (isMotionConsidered):
				if motion:
					self.logger.info("DelayTimerActionRule.process(): MOTION detected: stopping Timer")
					self.cancelTimer()
					self.isDelayTimerOn = False
				elif ((motion == False) and (self.isDelayTimerOn == False)) or (motion == False and self.isDelayTimerOn and oldPresence != presence):
					self.logger.info("DelayTimerActionRule.process(): NO MOTION detected: Changing Timer to %ld", self.defaultTimerInMills)
					self.cancelTimer()
					if self.defaultTimerInMills > 0:
						self.scheduleTimerTask(self.defaultTimerInMills)
					self.isDelayTimerOn = True
			else:
				if self.isDelayTimerOn == False:	
					self.cancelTimer()
					if self.defaultTimerInMills > 0:
						self.scheduleTimerTask(self.defaultTimerInMills)
					self.isDelayTimerOn = True
					self.logger.info("DelayTimerActionRule.process(): MOTION NOT CONSIDERED: Changing Timer to %ld", self.defaultTimerInMills)

				elif self.isDelayTimerOn and (oldPresence != presence):
					self.cancelTimer()
					if self.defaultTimerInMills > 0:
						self.scheduleTimerTask(self.defaultTimerInMills)
					self.isDelayTimerOn = True
					self.logger.info("DelayTimerActionRule.process(): MOTION NOT CONSIDERED: Changing Timer to %ld",self.defaultTimerInMills)
			
			self.setPresenceValue(presence)
			self.setMotionValue(motion)

	def stop(self):
		try:
			self.cancelTimer()
			self.isDelayTimerOn = False
		except Exception, e:
			self.logger.error("Error on DelayTimerActionRule.stop(): %s" % (e))  
	def scheduleTimerTask(self, timerInMinutes):
		self.timer = threading.Timer(float(timerInMinutes), self.run)
		self.timer.start()

	def cancelTimer(self):
		if hasattr(self, "timer"):
			try:
				self.timer.cancel()	
			except Exception, e:
				self.logger.error("Error on DelayTimerActionRule.cancelTimer(): %s" % (e)) 

	def toMinutes(self, value, isString=False):
		timer = 10
		if isString:
			try:
				timer = long(value)
			except ValueError, e:
				self.logger.error("Error on DelayTimerActionRule.toMinutes(). Setting defalut Timer = %ld mills. %s" %(timer, e)) 

		else:
			timer = value
		return timer * 60

	def setMotionValue(self, value):
		boolValue = False
		try:
			boolValue = Utilities.to_bool(value)
		except:
			self.logger.error("Error on DelayTimerActionRule.setMotionValue(). Setting defalut Motion = %s mills. %s" %(boolValue, e))

		self.context.updateProperty(ConfigurationConstants.getMotion(), str(boolValue))

	def setPresenceValue(self, value):
		boolValue = False
		try:
			boolValue = Utilities.to_bool(value)
		except:
			self.logger.error("Error on DelayTimerActionRule.setPresenceValue(). Setting defalut Presence = %s mills. %s" %(boolValue, e))

		self.context.updateProperty(ConfigurationConstants.getPresence(), str(boolValue))


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
