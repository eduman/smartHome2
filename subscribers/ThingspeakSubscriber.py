#!/usr/bin/env python

import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)

from abstractSubscriber.AbstractSubscriber import AbstractSubscriber
from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass
import logging
import os
import signal
import sys
import json
from smartHomeDevice import ActuationCommands
import threading
from threading import Thread
import time






subscriberName = "ThingspeakSubscriber"

homeWSUri = "http://localhost:8080/rest/home/configuration"
#homeWSUri = "http://192.168.1.5:8080/rest/home/configuration"


#logLevel = logging.INFO
logLevel = logging.DEBUG

class ThingspeakSubscriber (AbstractSubscriber):
	def __init__ (self):
		super(ThingspeakSubscriber, self).__init__(subscriberName, homeWSUri, "", logLevel)
		self.msgQueue = []
		self.__lock = threading.Lock()
		self.eventChannelMap = {}
		self.timer = 15 # sleep due to thingspeak limitations


	def mekeTopic(self, device, measureType):
		return EventTopics.getSensorMeasurementEvent() + "/" + str(device) + "/" + str(measureType).lower()

	def start (self):
		# method overrided 
		resp, isOk = self.invokeWebService(self.homeWSUri)
		while (not isOk):
			self.logger.error ("Unable to find the home proxy. I will try again in a while...")
			resp, isOk = self.invokeWebService(self.homeWSUri)
			time.sleep(10) #sleep 10 seconds

		myhome = json.loads(resp)
		brokerUri = myhome["homeMessageBroker"]["address"]
		brokerPort = myhome["homeMessageBroker"]["port"]
		if (brokerUri != None and brokerUri != "") and (brokerPort != None and brokerPort != ""):
			self.mqttc = MyMQTTClass(self.subscriberName, self.logger, self)
			self.mqttc.connect(brokerUri,brokerPort)
			for a, room in enumerate(myhome["rooms"]):
				for b, device in enumerate(room["devices"]):
					for c, channel in enumerate(device['thingspeakChannels']):
						topic = self.mekeTopic(device["deviceID"], channel['measureType'])
						self.eventChannelMap[topic] = channel['feed']
						event = self.mqttc.subscribeEvent(None, topic)
						self.subscribedEventList +=  event

		else:
			self.logger.error ("The message broker address is not valid")
	

		self.uploadThread = Thread (target = self.upload)
		self.uploadThread.start()
		self.loop()

	def stop (self):
		if (hasattr(self, "uploadThread")):
			if self.uploadThread.isAlive():
				try:
					self.uploadThread._Thread__stop()
				except:
					self.logger.error(str(self.uploadThread.getName()) + ' (upload value thread) could not terminated')

		super(ThingspeakSubscriber, self).stop()


	def upload(self):
		while (True):
			try:
				if (len(self.msgQueue) > 0):
					resp, isOk = self.invokeWebService(self.msgQueue[0])
					if isOk and resp is not "0":						
						self.__lock.acquire()
						deleted = self.msgQueue[0]
						del self.msgQueue[0]
						self.__lock.release()
						time.sleep(self.timer) # sleep due to thingspeak limitations
					else:
						self.logger.error ("Unable to upload new value: %s" % (resp))
				time.sleep(1)		
			except Exception, e:
				self.logger.error("Error on ThingspeakSubscriber.upload() %s: " % e)

			

	def notifyJsonEvent(self, topic, jsonEventString):
		self.logger.debug ("received topic: \"%s\" with msg: \"%s\"" % (topic, jsonEventString))

		try:
			self.__lock.acquire()
			data = json.loads(jsonEventString)
			if topic in self.eventChannelMap:
				timestamp = data["timestamp"].replace(" ", "T")
				uri = (self.eventChannelMap[topic] % (data["value"], timestamp))
				self.msgQueue.append(uri)
			else:
				self.logger.error("Feed not fond for topic %s: " % topic)

			self.__lock.release()
		except Exception, e:
			self.logger.error("Error on ThingspeakSubscriber.notifyJsonEvent() %s: " % e)


if __name__ == "__main__":
	ts = ThingspeakSubscriber()
	ts.start()