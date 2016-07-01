#!/usr/bin/env python

import logging
import os
import signal
import sys
import inspect
import threading
import time

import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import EventTopics
from myConfigurator import CommonConfigurator  
from smartHomeDevice import ActuationCommands
from utitlityLib import Utilities
from myConfigurator import CommonConfigurator  



class AbstractControlStategy(object):
	def __init__(self, strategyName, logLevel):
		#self.strategyName =  (inspect.stack()[0][1]).replace(".py", "").replace("./", "") #os.path.realpath(__file__) 
		self.strategyName = strategyName
		self.configPath = "../conf/controlStategies/%s.conf" % (self.strategyName)
		logPath = "../log/%s.log" % (self.strategyName)
		
		if not os.path.exists(logPath):
			try:
				os.makedirs(os.path.dirname(logPath))
			except Exception, e:
				pass	

		self.pid = str(os.getpid())
		self.pidfile = "/tmp/%s.pid" % (self.strategyName)


		self.logger = logging.getLogger(self.strategyName)
		self.logger.setLevel(logLevel)
		#self.logger.setLevel(logging.INFO)
		hdlr = logging.FileHandler(logPath)
		formatter = logging.Formatter(self.strategyName + ": " + "%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
		hdlr.setFormatter(formatter)
		self.logger.addHandler(hdlr)
		consoleHandler = logging.StreamHandler()
		consoleHandler.setFormatter(formatter)
		self.logger.addHandler(consoleHandler)

		self.subscribedEventList = []

		self.commonConfigPath = "../conf/microservice.conf"
		try:
			self.homeWSUri = CommonConfigurator.getHomeEndPointValue(self.commonConfigPath)
		except Exception, e:
			self.logger.error('Unable to start %s due to: %s' % (self.strategyName, e))
			self.stop()


	def signal_handler(self, signal, frame):
		self.stop()

	def loop(self):
		while (True):
			time.sleep(1.0)

	def stop (self):
		try:
			self.logger.info("Stopping %s" % (self.strategyName))
			self.ruleUpdater.stop()
			if hasattr(self, "timer"):
				try:
					self.timer.cancel()	
				except Exception, e:
					self.logger.error("Error on stop(): %s" % (e))

			if hasattr (self, "mqtt"):
				try:
					for event in self.subscribedEventList:
						self.mqtt.unsubscribeEvent(event)
					self.mqtt.disconnect()
				except Exception, e:
					self.logger.error("Error on stop(): %s" % (e))

			os.unlink(self.pidfile)
		except Exception, e2:
				self.logger.error("Error on stop(): %s" % (e2))
		sys.exit(0)


	def notifyJsonEvent(self, topic, jsonEventString):
		raise NotImplementedError('subclasses must override notifyJsonEvent(topic, jsonEventString)!')

	def setRuleEngine(self):
		raise NotImplementedError('subclasses must override setRuleEngine()!')

