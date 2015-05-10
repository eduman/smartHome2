#!/usr/bin/env python

from AbstractRule import AbstractRule
from Context import Context
import ConfigurationConstants
import ConfigParser
import logging
import os

class SaveStatusRule(AbstractRule):

	def __init__ (self, context, logger, path):
		super(SaveStatusRule, self).__init__(context, logger)
		self.path = path


	def process(self):
		try:
			config = ConfigParser.SafeConfigParser()
			if not os.path.exists(self.path):
				try:
					os.makedirs(os.path.dirname(self.path))
				except Exception, e:
					pass				

			f = open(self.path, "w+")
			config.add_section(ConfigurationConstants.getGeneralSettings())

			for keyword in ConfigurationConstants.getGeneralSettingsKeywords():
				value = self.context.getProperty(keyword)
				if value:
 					config.set(ConfigurationConstants.getGeneralSettings(), keyword, value)

			config.add_section(ConfigurationConstants.getRuleSettings())

			fullUserList = self.getContext().getProperty(ConfigurationConstants.getFullUserList())
			
			if fullUserList:
				users =  ''.join(fullUserList.split()).split(';')
				for u in users:
					if u:
						config.set(ConfigurationConstants.getRuleSettings(), u, self.getContext().getProperty(u))

			for keyword in ConfigurationConstants.getRuleSettingsKeywords():
				value = self.context.getProperty(keyword)
				if value:
 					config.set(ConfigurationConstants.getRuleSettings(), keyword, value)

			config.write(f)

		except Exception, e:
			self.logger.error ("Error on SaveStatusRule.process(): %s" % (e))

	def stop(self):
		pass

if __name__ == "__main__":
	context = Context(3)
	context.updateProperty(ConfigurationConstants.getFullUserList(), "ciao;edo;prova")
	context.updateProperty(ConfigurationConstants.getMessageBroker(), "broker")
	context.updateProperty("ciao", "True")
	context.updateProperty("edo", "True")
	context.updateProperty("prova", "false")
	context.updateProperty(ConfigurationConstants.getIsDelayTimerOn(), "True")
	logger = logging.getLogger('RestoreStatusRule')
	hdlr = logging.FileHandler('log/rule.log')
	formatter = logging.Formatter("%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
	hdlr.setFormatter(formatter)
	logger.addHandler(hdlr)

	rule = SaveStatusRule(context, logger, "conf/agents/rule.conf")
	rule.process()