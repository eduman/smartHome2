#!/usr/bin/env python

from Context import Context
from AbstractRule import AbstractRule

from RuleEngine import RuleEngine
import threading
import logging
import ConfigurationConstants


class RuleUpdater():
	def __init__(self, ruleEngine, logger):
		self.ruleEngine = ruleEngine
		self.logger = logger


	def start(self):
		try:
			self.timer = threading.Timer(300.0, self.run) #run after 5 minutes (300 seconds)
			self.timer.start()
		except Exception, e:
			self.logger.error("Error on RuleUpdater.start(): %s" % (e)) 

	def stop(self):
		try:
			self.logger.info("RuleUpdater.stop(): Stopping rule update scheduler.")
			if hasattr(self, "timer"):
				self.timer.cancel()
			self.ruleEngine.stop()
		except Exception, e:
			self.logger.error("Error on RuleUpdater.stop(): %s" % (e)) 

	def run(self):
		try:
			self.logger.info("RuleUpdater.run(): Periodic rule engine update.")
			self.ruleEngine.update()
			self.start()
		except Exception, e:
			self.logger.error("Error on RuleUpdater.run(): %s" % (e)) 



if __name__ == "__main__":
	
	logger = logging.getLogger('RestoreStatusRule')
	hdlr = logging.FileHandler('log/rule.log')
	formatter = logging.Formatter("%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
	hdlr.setFormatter(formatter)
	logger.addHandler(hdlr)

	context = Context(1)
	r1 = AbstractRule(context, logger)
	re = RuleEngine(context, logger)
	ru = RuleUpdater (re, logger)
	re.update()
	ru.start()
