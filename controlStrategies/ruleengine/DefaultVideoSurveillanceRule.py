#!/usr/bin/env python

from AbstractRule import AbstractRule
from Context import Context
import ConfigurationConstants
import ConfigParser
import logging
import os

class DefaultVideoSurveillanceRule(AbstractRule):

	def __init__ (self, context, logger, path):
		super(DefaultVideoSurveillanceRule, self).__init__(context, logger)
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
			self.logger.error ("Error on DefaultVideoSurveillaceRule.process(): %s" % (e))


	def stop(self):
		pass

	def setDefaultContext(self):
		config = ConfigParser.SafeConfigParser()
		config.read(self.path)

		if not self.context.getProperty(ConfigurationConstants.getPresence()):
			self.context.updateProperty(ConfigurationConstants.getPresence(), str(False))

		for key, value in config.items(ConfigurationConstants.getGeneralSettings()):
			self.getContext().updateProperty(key, value)

		for key, value in config.items(ConfigurationConstants.getRuleSettings()):
			self.getContext().updateProperty(key, value)




	def makeDefaultConfigFile(self):
		try:
			os.makedirs(os.path.dirname(self.path))
		except Exception, e:
			pass

		f = open(self.path, "w+")
		print self.path
		section = ConfigurationConstants.getGeneralSettings()

		
		ConfigParser.SafeConfigParser.add_comment = lambda self, section, option, value: self.set(section, '\n; '+option, value)
		config = ConfigParser.SafeConfigParser()
		config.add_section(section)

		key = ConfigurationConstants.getRuleSID()
		config.add_comment(section, "Insert the name for your rule eg. " + key, "VideoSurveillaceStrategy:UnknownOwner:Strategy")
		config.set(section, key, "VideoSurveillaceStrategy:UnknownOwner:Strategy")

		key = ConfigurationConstants.getRuleDescription()
		config.add_comment(section, "Insert a description for your rule eg. " + key, "Rule Description")
		config.set(section, key, "Rule Description")
		
		key = ConfigurationConstants.getMessageBroker()
		config.add_comment(section, "Insert the URI or IP address for your Message Broker eg. " + key, "tcp://localhost:1883")
		config.set(section, key, "localhost:1883")

		key = ConfigurationConstants.getRoomID()
		config.add_comment(section, "Insert your room ID eg. " + key, "RoomID")
		config.set(section, key, "RoomID")

		key = ConfigurationConstants.getIsRuleEnabled()
		config.add_comment(section, "Is the rule enabled? (True or False) eg. " + key, "True")
		config.set(section, key, "True")

		key = ConfigurationConstants.getFullActuatorList()
		config.add_comment(section, "Insert the video camera list (semicolon separeted) for the rule eg. " + key, "videoCam1;videoCam2;")
		config.set(section, key, "videoCam;videoCam;")


		config.add_section(ConfigurationConstants.getRuleSettings())

		config.write(f)

if __name__ == "__main__":
	context = Context(3)
	logger = logging.getLogger('DefaultVideoSurveillaceRule')
	hdlr = logging.FileHandler('log/rule.log')
	formatter = logging.Formatter("%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
	hdlr.setFormatter(formatter)
	logger.addHandler(hdlr)

	rule = DefaultVideoSurveillaceRule(context, logger, "conf/agents/rule.conf")
	rule.process()

