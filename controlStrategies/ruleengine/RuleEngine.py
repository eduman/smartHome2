#!/usr/bin/env python

from Context import Context
from AbstractRule import AbstractRule
from SaveStatusRule import SaveStatusRule
import threading
import logging
import ConfigurationConstants



class RuleEngine():

	def __init__(self, context, logger):
		self.rules = []
		self.context = context
		self.lock = threading.Lock()
		self.logger = logger


	def updateProperty (self, key, value):
		try:
			self.lock.acquire()
			self.context.updateProperty(key,value)
			self.lock.release() 
			self.process()
		except Exception, e:
			self.logger.error("Erroron RuleEngine.updateProperty(): %s" % (e))


	def addRule (self, rule):
		try:
			if (rule in self.rules) == False:
				self.rules.append(rule)
		except Exception, e:
			self.logger.error("Erroron RuleEngine.addRule(): %s" % (e))

	def update(self):
		try:
#			self.lock.acquire()
			self.process()
#			self.lock.release() 
		except Exception, e:
			self.logger.error("Erroron RuleEngine.update(): %s" % (e))

	def process(self):
		self.logger.info("RuleEngine: Processing rules...")
		self.lock.acquire()
		for rule in self.rules:
			try:
				rule.process()
			except Exception, e:
				self.logger.error("Error on RuleEngine.process(): %s" % (e))
		self.lock.release()

	def stop(self):
		self.logger.info("RuleEngine: Stopping rules.")
		self.lock.acquire()
		for rule in self.rules:
			try:
				if isinstance (rule, SaveStatusRule):
					rule.process()
				rule.stop()
			except Exception, e:
				self.logger.error("Erroron RuleEngine.stop(): %s" % (e))
		self.lock.release()


if __name__ == "__main__":
	
	logger = logging.getLogger('RestoreStatusRule')
	hdlr = logging.FileHandler('log/rule.log')
	formatter = logging.Formatter("%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
	hdlr.setFormatter(formatter)
	logger.addHandler(hdlr)

	context = Context(1)

	context.updateProperty(ConfigurationConstants.getFullUserList(), "ciao;edo;prova")
	context.updateProperty(ConfigurationConstants.getMessageBroker(), "broker")
	context.updateProperty("ciao", "True")
	context.updateProperty("edo", "True")
	context.updateProperty("prova", "false")
	context.updateProperty(ConfigurationConstants.getIsDelayTimerOn(), "True")
	

	#r1 = AbstractRule(context, logger)
	r1 = SaveStatusRule(context, logger, "conf/agents/rule.conf")
	re = RuleEngine(context, logger)
	re.addRule(r1)
	#re.update()
	re.stop()


