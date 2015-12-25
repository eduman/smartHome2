#!/usr/bin/python

import cherrypy
import logging
import json

import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)

from commons.myMqtt import MQTTPayload 
from commons.myMqtt.MQTTClient import MyMQTTClass
from commons.myMqtt import EventTopics
import datetime
import time




DEFAULT_BROKER_URI = "localhost"
DEFAULT_BROKER_PORT = "1883"

class SwitchOffAllDevicesAgent(object):
	exposed = True

	def __init__(self, serviceName, logLevel):
		self.serviceName = serviceName
		logPath = "log/%s.log" % (self.serviceName)
		self.confPath = "conf/%s.conf" % (self.serviceName)

		if not os.path.exists(logPath):
			try:
				os.makedirs(os.path.dirname(logPath))
			except Exception, e:
				pass	

		self.logger = logging.getLogger(self.serviceName)
		self.logger.setLevel(logLevel)
		hdlr = logging.FileHandler(logPath)
		formatter = logging.Formatter(self.serviceName + ": " + "%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
		hdlr.setFormatter(formatter)
		self.logger.addHandler(hdlr)
		
		consoleHandler = logging.StreamHandler()
		consoleHandler.setFormatter(formatter)
		self.logger.addHandler(consoleHandler)


	def start (self, cherrypyEngine, brokerUri=DEFAULT_BROKER_URI, brokerPort=DEFAULT_BROKER_PORT):
		#self.brokerUri = brokerUri
		#self.brokerPort = brokerPort

		self.brokerUri = "192.168.1.5"
		self.brokerPort = "1883"

		if hasattr(cherrypyEngine, 'signal_handler'):
			cherrypyEngine.signal_handler.subscribe()	
		cherrypyEngine.subscribe('stop', self.stop())

		self.mqtt = MyMQTTClass(self.serviceName, self.logger, self)
		self.mqtt.connect(self.brokerUri, self.brokerPort)


		self.logger.info("Started")



	def stop(self):
		if (hasattr (self, "mqtt")):
			try:
				self.mqtt.disconnect()
			except Exception, e:
				self.logger.error("Error on stop(): %s" % (e))

		self.logger.info("Ended")


	def GET(self, *ids, **params):
		result = ""
		if len(ids) > 0:
			param_0 = str(ids[0]).lower()
			if param_0 == "actuate":					
				timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
				payload = (MQTTPayload.getActuationPayload() %  (EventTopics.getSwitchOffAll(), "True", "switchoffall", "no_device", str(timestamp)))		
				self.mqtt.syncPublish(EventTopics.getSwitchOffAll(), payload, 2)
				result += payload

		else:
			self.logger.error("Command not found")
			raise cherrypy.HTTPError("404 Not found", "command not found")

	
		return result	

		
	def POST(self, *ids, **params):
		self.logger.error("Subclasses must override POST(self, *ids)!")
		raise NotImplementedError('subclasses must override POST(self, *ids)!')
		
	def PUT(self, *ids, **params):
		self.logger.error("Subclasses must override PUT(self, *ids)!")
		raise NotImplementedError('subclasses must override PUT(self, *ids)!')
		
	def DELETE(self, *ids, **params):
		self.logger.error("Subclasses must override DELETE(self, *ids)!")
		raise NotImplementedError('subclasses must override DELETE(self, *ids)!')
