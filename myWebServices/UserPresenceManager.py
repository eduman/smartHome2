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
from  threading import Thread





DEFAULT_BROKER_URI = "localhost"
DEFAULT_BROKER_PORT = "1883"

userJson = '{"user": "%s", "isPresent": %s}'



class UserPresenceManager(object):
	exposed = True

	def __init__(self, serviceName, logLevel, myHome):
		self.serviceName = serviceName
		self.myHome = myHome
		self.userList = {}
		logPath = "log/%s.log" % (self.serviceName)
		self.confPath = "conf/%s.conf" % (self.serviceName)
		self.event = EventTopics.getBehaviourProximity()
		self.timer = 300
		

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


		for rules in self.myHome['rules']:
			for user in rules['userList']:
				self.userList[user] = "false"

		if os.path.exists(self.confPath):
			try:
				json_data=open(self.confPath).read()
				data = json.loads(json_data)
				for userDict in data:	
#					if userDict['user'] in self.userList:
					self.userList[userDict['user']] = str(userDict['isPresent']).lower()
			except Exception, e:
				pass

		self.logger.info("Started")
			
	def stop(self):
		if hasattr (self, "mqtt"):
			try:
				self.mqtt.unsubscribeEvent(self.event)
				self.mqtt.disconnect()
			except Exception, e:
				self.logger.error("Error on stop(): %s" % (e))

		if (hasattr(self, "periodicUpdateThread")):
			if self.periodicUpdateThread.isAlive():
				try:
					self.periodicUpdateThread._Thread__stop()
				except:
					self.logger.error(str(self.periodicUpdateThread.getName()) + ' (periodic update send event thread) could not terminated')

		self.logger.info("Ended")


	def start (self, cherrypyEngine, brokerUri=DEFAULT_BROKER_URI, brokerPort=DEFAULT_BROKER_PORT):
		self.brokerUri = brokerUri
		self.brokerPort = brokerPort

		if hasattr(cherrypyEngine, 'signal_handler'):
			cherrypyEngine.signal_handler.subscribe()
		
		cherrypyEngine.subscribe('stop', self.stop())

		self.mqtt = MyMQTTClass(self.serviceName, self.logger, self)
		self.mqtt.connect(self.brokerUri, self.brokerPort)
		self.mqtt.subscribeEvent(None, self.event)

		self.periodicUpdateThread = Thread (target = self.loop)
		self.periodicUpdateThread.start()

	def loop (self):		
		while (True):
			for userName, presenceValue in self.userList.iteritems():
				timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
				topic = EventTopics.getBehaviourProximity() + "/" + userName
				payload = MQTTPayload.getActuationPayload() % (topic, str(presenceValue), "Proximity", userName, timestamp)
				self.mqtt.syncPublish(topic, payload, 2)				
			time.sleep(self.timer)

	def notifyJsonEvent(self, topic, jsonEventString):
		self.logger.debug ("received topic: \"%s\" with msg: \"%s\"" % (topic, jsonEventString))

		try: 
			data = json.loads(jsonEventString)
			self.userList[data['device']] = str(data['value']).lower()
			self.writeConfiguration(self.toJson())
		except Exception, e:
			self.logger.error("Error on UserPresenceManager.notifyJsonEvent() %s: " % e)

	def writeConfiguration(self, jsonStr):
		if not os.path.exists(self.confPath):
			try:
				os.makedirs(os.path.dirname(self.confPath))
			except Exception, e:
				pass	

		try:
			config = open(self.confPath, "w")
			config.write(jsonStr)
			config.close()
		except Exception, e:
			self.logger.error("Erron on writeConfiguration() %s", e)

	def toJson (self):
		result = "["
		iteration = len(self.userList)
		if iteration > 0:
			for key, value in self.userList.iteritems():
				iteration -= 1
				result += (userJson % (key, str(value)))
				if iteration > 0:
					result += ","

		result += "]"
		return result


	def GET(self, *ids, **params):
		result = ""
		if len(ids) > 0:
			ids_0 = str(ids[0]).lower()
			if ids_0 == "presence" and len(params) == 0:			
				result += self.toJson()
			elif ids_0 == "presence" and len(params) == 2:
				isOk = False
				userName = params["user"]
				presenceValue = params["isPresent"]

				if userName is not None and presenceValue is not None:
					if (presenceValue.lower() == "true" or presenceValue.lower() == "1"):
						presenceValue = "True"
						isOk = True
					elif (presenceValue.lower() == "false" or presenceValue.lower() == "0"):
						presenceValue = "False"
						isOk = True
				if isOk:
					timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
					topic = EventTopics.getBehaviourProximity() + "/" + userName
					payload = MQTTPayload.getActuationPayload() % (topic, presenceValue, "Proximity", userName, timestamp)
					self.mqtt.syncPublish(topic, payload, 2)
				else:
					self.logger.error("Parameters not valid")
					raise cherrypy.HTTPError("404 Not found", "Parameters not valid")
			else:
				self.logger.error("Command not found")
				raise cherrypy.HTTPError("404 Not found", "command not found")

		else:
			self.logger.error("Command not found")
			raise cherrypy.HTTPError("404 Not found", "command not found")

		return result
		

		
	def POST(self, *ids):
		self.logger.error('Subclasses must override POST(self, *ids)!')
		raise NotImplementedError('subclasses must override POST(self, *ids)!')
		
	def PUT(self, *ids):
		self.logger.error('Subclasses must override PUT(self, *ids)!')
		raise NotImplementedError('subclasses must override PUT(self, *ids)!')
		
	def DELETE(self, *ids):
		self.logger.error('Subclasses must override DELETE(self, *ids)!')
		raise NotImplementedError('subclasses must override DELETE(self, *ids)!')