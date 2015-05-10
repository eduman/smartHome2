#!/usr/bin/env python

from AbstractRule import AbstractRule
from Context import Context
import ConfigurationConstants
import ConfigParser
import logging
import os

class DefaultTimerRule(AbstractRule):

	def __init__ (self, context, logger, path):
		super(DefaultTimerRule, self).__init__(context, logger)
		self.isBeginning = True
		self.path = path

	def process(self):
		try: 
			if self.isBeginning:
				if not os.path.exists(self.path):
					self.makeDefaultConfigFile()

				self.setDefaultContext()
				self.isBeginning = False
		except Exception, e:
			self.logger.error ("Error on DefaultTimerRules.process(): %s" % (e))


	def stop(self):
		pass

	def setDefaultContext(self):
		config = ConfigParser.SafeConfigParser()
		config.read(self.path)

		toBeSplitted = [ConfigurationConstants.getFullSensorList(), ConfigurationConstants.getFullActuatorList(), ConfigurationConstants.getFullUserList()]

		if not self.context.getProperty(ConfigurationConstants.getIsDelayTimerOn()):
			self.context.updateProperty(ConfigurationConstants.getIsDelayTimerOn(), str(False))

		if not self.context.getProperty(ConfigurationConstants.getMotion()):
			self.context.updateProperty(ConfigurationConstants.getMotion(), str(False))

		if not self.context.getProperty(ConfigurationConstants.getPresence()):
			self.context.updateProperty(ConfigurationConstants.getPresence(), str(False))

		for key, value in config.items(ConfigurationConstants.getGeneralSettings()):
			self.getContext().updateProperty(key, value)
			if key in toBeSplitted:
				tokens  =  ''.join(value.split()).split(';')
				for tok in tokens:
					if tok:
						self.context.updateProperty(tok, "False")

		for key, value in config.items(ConfigurationConstants.getRuleSettings()):
			self.getContext().updateProperty(key, value)




	def makeDefaultConfigFile(self):
		try:
			os.makedirs(os.path.dirname(self.path))
		except Exception, e:
			pass

		f = open(self.path, "w+")
		section = ConfigurationConstants.getGeneralSettings()

		
		ConfigParser.SafeConfigParser.add_comment = lambda self, section, option, value: self.set(section, '\n; '+option, value)
		config = ConfigParser.SafeConfigParser()
		config.add_section(section)

		key = ConfigurationConstants.getRuleSID()
		config.add_comment(section, "Insert the name for your rule eg. " + key, "TimerControlStrategy:UnknownOwner:Strategy")
		config.set(section, key, "TimerControlStrategy:UnknownOwner:Strategy")

		key = ConfigurationConstants.getRuleDescription()
		config.add_comment(section, "Insert a description for your rule eg. " + key, "Rule Description")
		config.set(section, key, "Rule Description")
		
		key = ConfigurationConstants.getMessageBroker()
		config.add_comment(section, "Insert the URI or IP address for your Message Broker eg. " + key, "tcp://localhost:1883")
		config.set(section, key, "localhost:1883")

		key = ConfigurationConstants.getRoomID()
		config.add_comment(section, "Insert your room ID eg. " + key, "RoomID")
		config.set(section, key, "RoomID")

		key = ConfigurationConstants.getFullUserList()
		config.add_comment(section, "Insert the user list (semicolon separeted) for the rule owner(s) eg. " + key, "guest;user1;user2;")
		config.set(section, key, "guest;user1;user2;")

		key = ConfigurationConstants.getFullSensorList()
		config.add_comment(section, "Insert the sensor list (semicolon separeted) for the rule eg. " + key, "sensor1;sensor2;")
		config.set(section, key, "sensor1;sensor2;")

		key = ConfigurationConstants.getFullActuatorList()
		config.add_comment(section, "Insert the actuator list (semicolon separeted) for the rule eg. " + key, "actuator1;actuator2;")
		config.set(section, key, "actuator1;actuator2;")
		
		key = ConfigurationConstants.getFullButtonList()
		config.add_comment(section, "Insert the button list (semicolon separeted) for the rule eg. " + key, "button1;button2;")
		config.set(section, key, "button1;button2;")

		key = ConfigurationConstants.getAbsenceTimer()
		config.add_comment(section, "Insert timer for absence periods in minutes (-1 infinite timer) eg. " + key, "5")
		config.set(section, key, "5")

		key = ConfigurationConstants.getPresenceTimer()
		config.add_comment(section, "Insert timer for presence periods in minutes (-1 infinite timer) eg. " + key, "10")
		config.set(section, key, "10")

		key = ConfigurationConstants.getIsRuleEnabled()
		config.add_comment(section, "Is the rule enabled? (True or False) eg. " + key, "True")
		config.set(section, key, "True")



		config.add_section(ConfigurationConstants.getRuleSettings())

		config.write(f)

if __name__ == "__main__":
	context = Context(3)
	logger = logging.getLogger('RestoreStatusRule')
	hdlr = logging.FileHandler('log/rule.log')
	formatter = logging.Formatter("%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
	hdlr.setFormatter(formatter)
	logger.addHandler(hdlr)

	rule = DefaultTimerRule(context, logger, "conf/agents/rule.conf")
	rule.process()

