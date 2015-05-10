#!/usr/bin/env python

from Context import Context
from time import gmtime, strftime
import logging


class AbstractRule(object):

	def __init__(self, context, logger):
		self.context = context
		self.logger = logger


	def getContext(self):
		return self.context

	def getCurrentDate(self):
		return strftime("%Y-%m-%d %H:%M:%S", gmtime())

	def stop(self):
		raise NotImplementedError('subclasses must override stop()!')

	def process (self):
		raise NotImplementedError('subclasses must override process()!')



if __name__== "__main__":
	logger = logging.getLogger('RestoreStatusRule')
	hdlr = logging.FileHandler('log/rule.log')
	formatter = logging.Formatter("%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
	hdlr.setFormatter(formatter)
	logger.addHandler(hdlr)

	context = Context(3)
	rule = AbstractRule(context, logger)
	print (rule.getContext().getID())
	print (rule.getContext().getKeys())
	print rule.getCurrentDate()