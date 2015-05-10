#!/usr/bin/env python

from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass
import logging
import os
import signal
import sys
#import inspect
import json
import time

import urllib
import urllib2
import logging
import httplib


class AbstractSubscriber(object):
	def __init__(self, subscriberName, logLevel):
		self.subscriberName = subscriberName
		self.configPath = "conf/agents/%s.conf" % (self.subscriberName)
		logPath = "log/%s.log" % (self.subscriberName)

		if not os.path.exists(logPath):
			try:
				os.makedirs(os.path.dirname(logPath))
			except Exception, e:
				pass	

		self.logger = logging.getLogger(self.subscriberName)
		self.logger.setLevel(logLevel)
		#self.logger.setLevel(logging.INFO)
		hdlr = logging.FileHandler(logPath)
		formatter = logging.Formatter(self.subscriberName + ": " + "%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
		hdlr.setFormatter(formatter)
		self.logger.addHandler(hdlr)
		
		consoleHandler = logging.StreamHandler()
		consoleHandler.setFormatter(formatter)
		self.logger.addHandler(consoleHandler)


		for sig in (signal.SIGABRT, signal.SIGILL, signal.SIGINT, signal.SIGSEGV, signal.SIGTERM):
			signal.signal(sig, self.signal_handler)


	def signal_handler(self, signal, frame):
		self.stop()

	def loop(self):
		while (True):
			time.sleep(1.0)

	def stop (self):
		self.logger.info("Stopping %s" % (self.subscriberName))
		if hasattr (self, "mqttc"):
			try:
				self.mqttc.disconnect()
			except Exception, e:
				self.logger.error("Error on stop(): %s" % (e))

		sys.exit(0)

	def invokeWebService (self, uri):
		try:
			req = urllib2.Request(uri)
			req.add_header('Content-Type', 'application/json')
			resp = urllib2.urlopen(req).read()
			return resp

		except urllib2.HTTPError, e:
		    self.logger.error('HTTPError = %s.' % e )
		except urllib2.URLError, e:
		    self.logger.error('URLError = %s.' % e )
		except httplib.HTTPException, e:
		    self.logger.error('HTTPException = %s.' % e)
		except Exception, e:
			self.logger.error('generic exception: %s.' % e )

	def notifyJsonEvent(self, topic, jsonEventString):
		raise NotImplementedError('subclasses must override notifyJsonEvent(topic, jsonEventString)!')
		
