#!/usr/bin/env python

from AbstractRule import AbstractRule
from Context import Context
import ConfigurationConstants
import ConfigParser
import ConfigurationConstants
import logging
import os






class RestoreStatusRule(AbstractRule):

	def __init__ (self, context, logger, path):
		super(RestoreStatusRule, self).__init__(context, logger)
		self.isBeginning = True
		self.path = path


	def process(self):
		if self.isBeginning:
			try:
				config = ConfigParser.SafeConfigParser()

				if os.path.exists(self.path):
					config.read(self.path)
					for key, value in config.items(ConfigurationConstants.getRuleSettings()):
						self.getContext().updateProperty(key, value)

					self.getContext().updateProperty(ConfigurationConstants.getIsDelayTimerOn(), str(False));
				
				self.isBeginning = False

			except Exception, e:
				self.logger.error ("Error on RestoreStatusRule.process(): %s" % (e))

	def stop(self):
		pass

if __name__ == "__main__":
	context = Context(3)
	logger = logging.getLogger('RestoreStatusRule')
	hdlr = logging.FileHandler('log/rule.log')
	formatter = logging.Formatter("%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
	hdlr.setFormatter(formatter)
	logger.addHandler(hdlr)

	rule = RestoreStatusRule(context, logger, "conf/agents/rule.conf")
	rule.process()