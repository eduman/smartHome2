#!/usr/bin/env python

from AbstractRule import AbstractRule
from Context import Context
import ConfigurationConstants
import logging



class InititializationRule(AbstractRule):
	def __init__ (self, context, logger):
		super(InititializationRule, self).__init__(context, logger)

	def process(self):
		try:
			self.getContext().updateProperty(ConfigurationConstants.getLastExecutionDate(), self.getCurrentDate())
		except Exception, e:
			self.logger.error("Erroron InititializationRule.process(): %s" % (e))	
		

	def stop(self):
		pass


if __name__ == "__main__":
	logger = logging.getLogger('RestoreStatusRule')
	hdlr = logging.FileHandler('log/rule.log')
	formatter = logging.Formatter("%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
	hdlr.setFormatter(formatter)
	logger.addHandler(hdlr)


	context = Context(3)
	rule = InititializationRule(context, logger)

	print (rule.getContext().getID())
	print (rule.getContext().getKeys())
	rule.process()
	print rule.getContext().getProperties()
	rule.stop()