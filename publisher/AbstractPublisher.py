#!/usr/bin/python

import cherrypy
import logging
import os
import datetime
import time


import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)

#from myMqtt import MQTTPayload 
#from myMqtt.MQTTClient import MyMQTTClass
from commons.myMqtt import MQTTPayload 
from commons.myMqtt.MQTTClient import MyMQTTClass


DEFAULT_BROKER_URI = "localhost"
DEFAULT_BROKER_PORT = "1883"

class AbstractPublisher(object):

	def __init__(self, serviceName, logLevel):
		self.serviceName = serviceName
		logPath = "../log/%s.log" % (self.serviceName)
		

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
		self.brokerUri = brokerUri
		self.brokerPort = brokerPort

		if hasattr(cherrypyEngine, 'signal_handler'):
			cherrypyEngine.signal_handler.subscribe()
		
		cherrypyEngine.subscribe('stop', self.stop())

		self.mqtt = MyMQTTClass(self.serviceName, self.logger, self)
		self.mqtt.connect(self.brokerUri, self.brokerPort)


	def stop(self):
		if hasattr (self, "mqtt"):
			try:
				self.mqtt.disconnect()
			except Exception, e:
				self.logger.error("Error on stop(): %s" % (e))

	def makeActionEvent(self, controlEventTopic, deviceId, function):
		return controlEventTopic + "/" + deviceId + "/" + function

	def publishEvent(self, topic, device, event, value): 
		timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
		payload = (MQTTPayload.getActuationPayload() %  (str(topic), str(value), str(event), str(device), str(timestamp)))
		self.logger.debug ("publishing: %s" % payload) 
		self.mqtt.publish(topic, payload, 2)
		return payload


	def GET(self, **params):
		raise NotImplementedError('subclasses must override GET(self, **params)!')
		
	def POST(self, **params):
		raise NotImplementedError('subclasses must override notifyJsonEvent(topic, jsonEventString)!')
		
	def PUT(self, **params):
		raise NotImplementedError('subclasses must override notifyJsonEvent(topic, jsonEventString)!')
		
	def DELETE(self, **params):
		raise NotImplementedError('subclasses must override notifyJsonEvent(topic, jsonEventString)!')
		


