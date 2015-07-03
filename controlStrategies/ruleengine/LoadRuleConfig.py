#!/usr/bin/env python

import json
import time
import urllib
import urllib2
import logging
import httplib
import ConfigurationConstants
import ConfigParser
import logging
import os
import sys

from AbstractRule import AbstractRule
from Context import Context


class LoadRuleConfig(AbstractRule):

	def __init__ (self, context, logger, path, homeWSUri, ruleSID):
		super(LoadRuleConfig, self).__init__(context, logger)
		self.isBeginning = True
		self.path = path
		self.homeWSUri = homeWSUri
		self.ruleSID = ruleSID


	def process(self):
		#try: 
		if self.isBeginning:
			resp, isOk = self.invokeWebService(self.homeWSUri)
			while (not isOk):
				self.logger.error ("Unable to find the home proxy. I will try again in a while...")
				resp, isOk = self.invokeWebService(self.homeWSUri)
				time.sleep(10) #sleep 10 seconds

			myhome = json.loads(resp)
			self.loadDefaultConfigFile(myhome)
			#self.setDefaultContext()
			self.isBeginning = False
		#except Exception, e:
		#	self.logger.error ("Error on LoadRuleConfig.process(): %s" % (e))


	def stop(self):
		pass

	def caseInsensitive(self, jsonDict):
		return dict(map(lambda (key, value):(key.lower(),value), jsonDict.items()))



	def invokeWebService (self, uri):
		try:
			req = urllib2.Request(uri)
			req.add_header('Content-Type', 'application/json')
			resp = urllib2.urlopen(req).read()
			return resp, True

		except urllib2.HTTPError, e:
		    self.logger.error('HTTPError = %s.' % e )
		    return "HTTPError", False
		except urllib2.URLError, e:
		    self.logger.error('URLError = %s.' % e )
		    return "URLError", False
		except httplib.HTTPException, e:
		    self.logger.error('HTTPException = %s.' % e)
		    return "HTTPException", False
		except Exception, e:
			self.logger.error('generic exception: %s.' % e )
			return "generic exception", False

	def updatePropertyFromOldConfig (self, section, key, oldConfig):
		if oldConfig.has_option(section, key):
			self.getContext().updateProperty(key, oldConfig.get(section, key))

	def loadDefaultConfigFile(self, myhome):
		found = False
		for rule in myhome["rules"]:
			rule = self.caseInsensitive(rule)
		
			if (rule[ConfigurationConstants.getRuleSID()] == self.ruleSID):

				config = ConfigParser.SafeConfigParser()
				oldConfig = ConfigParser.SafeConfigParser()

				if not os.path.exists(self.path):
					try:
						os.makedirs(os.path.dirname(self.path))
					except Exception, e:
						pass
				else: 
					oldConfig.read(self.path)
				
				f = open(self.path, "w+")
				
				
				#general settings section
				section = ConfigurationConstants.getGeneralSettings()				
				#config = ConfigParser.SafeConfigParser()
				config.add_section(section)

				key = ConfigurationConstants.getMessageBroker()
				broker = "%s:%s" %(str(myhome["homeMessageBroker"]["address"]), str(myhome["homeMessageBroker"]["port"]))
				config.set(section, key, broker)
				self.context.updateProperty(key, broker)

				key = ConfigurationConstants.getExternalMessageBroker()
				broker = "%s:%s" %(str(myhome["externalMessageBroker"]["address"]), str(myhome["externalMessageBroker"]["port"]))
				config.set(section, key, broker)
				self.context.updateProperty(key, broker)

				key = ConfigurationConstants.getRuleSID()
				config.set(section, key, str(rule[key]))
				self.context.updateProperty(key, str(rule[key]))

				key = ConfigurationConstants.getRuleDescription()
				config.set(section, key, str(rule[key]))
				self.context.updateProperty(key, str(rule[key]))

				key = ConfigurationConstants.getIsRuleEnabled()
				config.set(section, key, str(rule[key]))
				self.context.updateProperty(key, str(rule[key]))

				key = ConfigurationConstants.getRoomID()
				config.set(section, key, str(rule[key]))
				self.context.updateProperty(key, str(rule[key]))

				key = ConfigurationConstants.getPresenceTimer()
				config.set(section, key, str(rule[key]))
				self.context.updateProperty(key, str(rule[key]))

				key = ConfigurationConstants.getAbsenceTimer()
				config.set(section, key, str(rule[key]))
				self.context.updateProperty(key, str(rule[key]))


				#populating also rule_settings section
				key = ConfigurationConstants.getFullActuatorList()
				actuatorList = ""
				for device in rule[key]:
					devStr = str(device["deviceID"])
					actuatorList += ("%s;" % devStr)
					self.context.updateProperty(devStr, "False")
					self.updatePropertyFromOldConfig (ConfigurationConstants.getRuleSettings(), devStr, oldConfig)
				if (actuatorList != ""):
					config.set(section, key, actuatorList)
					self.context.updateProperty(key, actuatorList)

				key = ConfigurationConstants.getFullUserList()
				userList = ""
				for user in rule[key]:
					userList += ("%s;" % user)
					self.context.updateProperty(user, "False")
					self.updatePropertyFromOldConfig (ConfigurationConstants.getRuleSettings(), user, oldConfig)
				if (userList != ""):
					config.set(section, key, userList)
					self.context.updateProperty(key, userList)
				

				key = ConfigurationConstants.getFullSensorList()
				sensorList = ""
				for sensor in rule[key]:
					sensorList += ("%s;" % sensor)
					self.context.updateProperty(sensor, "False")
					self.updatePropertyFromOldConfig (ConfigurationConstants.getRuleSettings(), sensor, oldConfig)
				if (sensorList != ""):
					config.set(section, key, sensorList)
					self.context.updateProperty(key, sensorList)

				key = ConfigurationConstants.getFullButtonList()
				buttonList = ""
				for button in rule[key]:
					buttonList += ("%s;" % button)
					self.context.updateProperty(button, "False")
					self.updatePropertyFromOldConfig (ConfigurationConstants.getRuleSettings(), button, oldConfig)
				if (buttonList != ""):
					config.set(section, key, buttonList)
					self.context.updateProperty(key, buttonList)
				

				#time shift sections
				timeShifts = rule["timeshifts"]
				for shift in timeShifts:
					section = shift["day"]
					config.add_section(section)
					config.set(section, ConfigurationConstants.getStartTime(), shift["start_time"])
					config.set(section, ConfigurationConstants.getEndTime(), shift["end_time"])
					self.getContext().updateProperty( section+"_"+ConfigurationConstants.getStartTime(), shift["start_time"])
					self.getContext().updateProperty( section+"_"+ConfigurationConstants.getEndTime(), shift["end_time"])


				config.add_section(ConfigurationConstants.getRuleSettings())
				config.write(f)



				if not self.context.getProperty(ConfigurationConstants.getIsDelayTimerOn()):
					self.context.updateProperty(ConfigurationConstants.getIsDelayTimerOn(), str(False))

				if not self.context.getProperty(ConfigurationConstants.getMotion()):
					self.context.updateProperty(ConfigurationConstants.getMotion(), str(False))

				if not self.context.getProperty(ConfigurationConstants.getPresence()):
					self.context.updateProperty(ConfigurationConstants.getPresence(), str(False))

				self.getContext().updateProperty(ConfigurationConstants.getWeekdayOffState(), "False")
				self.getContext().updateProperty(ConfigurationConstants.getWeekdayOnState(), "False")

				#for key, value in config.items(ConfigurationConstants.getRuleSettings()):
				#	self.getContext().updateProperty(key, value)


				found = True


		if (not found):
			self.logger.error ("RuleSID = %s not found in myHome json", self.ruleSID)
			sys.exit()





#	def setDefaultContext(self):
#		config = ConfigParser.SafeConfigParser()
#		config.read(self.path)
#
#		toBeSplitted = [ConfigurationConstants.getFullSensorList(), ConfigurationConstants.getFullActuatorList(), ConfigurationConstants.getFullUserList()]
#
#		if not self.context.getProperty(ConfigurationConstants.getIsDelayTimerOn()):
#			self.context.updateProperty(ConfigurationConstants.getIsDelayTimerOn(), str(False))
#
#		if not self.context.getProperty(ConfigurationConstants.getMotion()):
#			self.context.updateProperty(ConfigurationConstants.getMotion(), str(False))
#
#		if not self.context.getProperty(ConfigurationConstants.getPresence()):
#			self.context.updateProperty(ConfigurationConstants.getPresence(), str(False))
#
#		for key, value in config.items(ConfigurationConstants.getGeneralSettings()):
#			self.getContext().updateProperty(key, value)
#			if key in toBeSplitted:
#				tokens  =  ''.join(value.split()).split(';')
#				for tok in tokens:
#					if tok:
#						self.context.updateProperty(tok, "False")
#
#		for section in ConfigurationConstants.getWeekdaysSection():
#			if config.has_section(section):
#				for key, value in config.items(section):
#					if key and value:
#						self.getContext().updateProperty( section+"_"+key, value)
#
#		for key, value in config.items(ConfigurationConstants.getRuleSettings()):
#			self.getContext().updateProperty(key, value)
#
#		self.getContext().updateProperty(ConfigurationConstants.getWeekdayOffState(), "False")
#		self.getContext().updateProperty(ConfigurationConstants.getWeekdayOnState(), "False")



