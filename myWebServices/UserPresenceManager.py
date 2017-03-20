#!/usr/bin/python

import cherrypy
import logging
import json
import os, sys
import datetime
import time
from  threading import Thread

import abstractAgent.AbstractAgent as AbstractAgent
from abstractAgent.AbstractAgent import AbstractAgentClass

lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import MQTTPayload 
from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass  
from mySSLUtil import MySSLUtil



DEFAULT_BROKER_URI = "localhost"
DEFAULT_BROKER_PORT = "1883"

httpPort = 8087
#httpPort = 443
#logLevel = logging.DEBUG
logLevel = logging.INFO


userJson = '{"user": "%s", "isPresent": %s}'



class UserPresenceManager(AbstractAgentClass):
	exposed = True

	def __init__(self, serviceName, logLevel):
		super(UserPresenceManager, self).__init__(serviceName, logLevel)
		self.confPath = "../conf/%s.conf" % (self.serviceName)
		self.myhome = self.retriveHomeSettings()
		self.brokerUri = self.myhome["homeMessageBroker"]["address"]
		self.brokerPort = self.myhome["homeMessageBroker"]["port"]
		self.userList = {}
		self.event = EventTopics.getBehaviourProximity()
		self.timer = 300

		for rules in self.myhome['rules']:
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

	def getMountPoint(self):
		return '/rest/userpresence'

	def start (self):
		self.mqtt = MyMQTTClass(self.serviceName, self.logger, self)
		self.mqtt.connect(self.brokerUri, self.brokerPort)
		self.mqtt.subscribeEvent(None, self.event)

		self.periodicUpdateThread = Thread (target = self.loop)
		self.periodicUpdateThread.start()
			
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

if __name__ == "__main__":
	upm = UserPresenceManager("UserPresenceManager", logLevel)
	AbstractAgent.startCherrypy(httpPort, upm)


