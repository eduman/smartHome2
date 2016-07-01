#!/usr/bin/env python

import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)

from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass
from myConfigurator import CommonConfigurator  

import logging
import os
import signal
import sys
import json
import time

import urllib
import urllib2
import logging
import httplib


class AbstractSubscriber(object):
	def __init__(self, subscriberName, deviceType, logLevel):
		self.subscriberName = subscriberName
		self.deviceType = deviceType
		self.configPath = "conf/agents/%s.conf" % (self.subscriberName)
		logPath = "../log/%s.log" % (self.subscriberName)

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

		self.subscribedEventList = []


		for sig in (signal.SIGABRT, signal.SIGILL, signal.SIGINT, signal.SIGSEGV, signal.SIGTERM):
			signal.signal(sig, self.signal_handler)

		self.commonConfigPath = "../conf/microservice.conf"
		try:
			self.homeWSUri = CommonConfigurator.getHomeEndPointValue(self.commonConfigPath)
		except Exception, e:
			self.logger.error('Unable to start %s due to: %s' % (self.subscriberName, e))
			self.stop()


	def signal_handler(self, signal, frame):
		self.stop()

	def loop(self):
		while (True):
			time.sleep(1.0)

	def start (self):
		resp, isOk = self.invokeWebService(self.homeWSUri)
		while (not isOk):
			self.logger.error ("Unable to find the home proxy. I will try again in a while...")
			resp, isOk = self.invokeWebService(self.homeWSUri)
			time.sleep(10) #sleep 10 seconds

		myhome = json.loads(resp)
		actuators = []
		for i, rule in enumerate(myhome['rules']):				
			for i, device in enumerate(rule['actuatorList']):
				dev = device['deviceID'].lower()
				if (device['type'].lower() == self.deviceType.lower()) and (dev not in actuators):
					actuators.append(dev)
		
		
		brokerUri = myhome["homeMessageBroker"]["address"]
		brokerPort = myhome["homeMessageBroker"]["port"]
		if (brokerUri != None and brokerUri != "") and (brokerPort != None and brokerPort != ""):
			self.mqttc = MyMQTTClass(self.subscriberName, self.logger, self)
			self.mqttc.connect(brokerUri,brokerPort)
			for a in actuators:
				event = self.mqttc.subscribeEvent(a, EventTopics.getActuatorAction())
				self.subscribedEventList +=  event
		else:
			self.logger.error ("The message broker address is not valid")
	
		self.loop()


	def stop (self):
		self.logger.info("Stopping %s" % (self.subscriberName))
		if hasattr (self, "mqttc"):
			try:
				for event in self.subscribedEventList:
					self.mqttc.unsubscribeEvent(event)	
				self.mqttc.disconnect()
			except Exception, e:
				self.logger.error("Error on stop(): %s" % (e))

		sys.exit(0)

	def invokeWebService (self, uri):
		try:
			req = urllib2.Request(uri)
			req.add_header('Content-Type', 'application/json')
			resp = urllib2.urlopen(req).read()
			return resp, True

		except urllib2.HTTPError, e:
		    self.logger.error('HTTPError = %s.' % e )
		    return "HTTPError", False
		except urllib2.URLError, e:
		    self.logger.error('URLError = %s.' % e )
		    return "URLError", False
		except httplib.HTTPException, e:
		    self.logger.error('HTTPException = %s.' % e)
		    return "HTTPException", False
		except Exception, e:
			self.logger.error('generic exception: %s.' % e )
			return "generic exception", False

	def notifyJsonEvent(self, topic, jsonEventString):
		raise NotImplementedError('subclasses must override notifyJsonEvent(topic, jsonEventString)!')
		
