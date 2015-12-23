#!/usr/bin/python
# -*- coding: iso-8859-15 -*-
import cherrypy
import logging
import os
import datetime
import time
import os, sys
import subprocess
import Adafruit_DHT
import RPi.GPIO as gpio
from  threading import Thread
import json
from commons.myMqtt import MQTTPayload 
from commons.myMqtt import EventTopics
from commons.myMqtt.MQTTClient import MyMQTTClass

DEFAULT_BROKER_URI = "localhost"
DEFAULT_BROKER_PORT = "1883"


class RaspberryAgent(object):
	exposed = True
	
	def __init__(self, serviceName, logLevel, ipAddress, port, timer):
		self.serviceName = serviceName
		self.ipAddress = ipAddress
		self.port = port

		#About DHT sensor
		self.dhtPin = 18
		self.timer = timer
		self.isDHTInstalled = False
#		self.isLoop = True
		self.dhtType = Adafruit_DHT.DHT22


		#About PIR sensor
		self.isPirInstalled = False
		self.pirPin = 7			

		self.WSUri = ("http://%s:%s/rest/raspberry/" % (self.ipAddress, str(self.port)))
		logPath = "log/%s.log" % (self.serviceName)
		
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


	def setDHTPin(self, dhtPin):
		self.dhtPin = dhtPin

	def setDHTType (self, dhtType):
		if (dhtType == 11):
			self.dhtType = Adafruit_DHT.DHT11
		elif (dhtType == 22):
			self.dhtType = Adafruit_DHT.DHT22
		elif (dhtType == 2302):
			self.dhtType = Adafruit_DHT.Adafruit_DHT.AM2302

	def setDHTInstalled (self, isDHTInstalled):
		self.isDHTInstalled = isDHTInstalled

	def setPirPin (self, pirPin):
		self.pirPin = pirPin

	def setPirInstalled (self, isPirInstalled):
		self.isPirInstalled = isPirInstalled

	def start (self, cherrypyEngine, brokerUri=DEFAULT_BROKER_URI, brokerPort=DEFAULT_BROKER_PORT):
		self.brokerUri = brokerUri
		self.brokerPort = brokerPort

		if hasattr(cherrypyEngine, 'signal_handler'):
			cherrypyEngine.signal_handler.subscribe()	
		cherrypyEngine.subscribe('stop', self.stop())

		self.mqtt = MyMQTTClass(self.serviceName, self.logger, self)
		self.mqtt.connect(self.brokerUri, self.brokerPort)

		if self.isPirInstalled:
			gpio.setmode(gpio.BOARD)
			gpio.setup(self.pirPin, gpio.IN)
			gpio.add_event_detect(self.pirPin, gpio.RISING, callback=self.pirCallback, bouncetime=200)

		if self.isDHTInstalled:
			self.dhtThread = Thread (target = self.loop)
			self.dhtThread.start()

		self.logger.info("Started")

	def loop (self):		
		while (True):
			humVal, tempVal = self.getDHTValues()	
			if humVal is not "0.0":				
				topic, payload =  self.makeEvent("humidity", humVal)
				self.mqtt.syncPublish(topic, payload, 2)
			if tempVal is not "0.0":
				topic, payload =  self.makeEvent("temperature", tempVal)
				self.mqtt.syncPublish(topic, payload, 2)
			time.sleep(self.timer)

	def getDHTValues (self):
		humidity = None
		temperature = None
		
		if self.isDHTInstalled:
			humidity, temperature = Adafruit_DHT.read_retry(self.dhtType, self.dhtPin)
			
		if humidity is None:
			humidity = 0.0

		if temperature is None:
			temperature = 0.0

		return str(round(humidity, 2)), str(round(temperature, 2))


	def pirCallback (self, pin):
		topic, payload = self.makeEvent("motion", "True")
		self.mqtt.syncPublish(topic, payload, 2)

	def stop(self):
		if (hasattr(self, "dhtThread")):
			if self.dhtThread.isAlive():
				try:
					self.dhtThread._Thread__stop()
				except:
					self.logger.error(str(self.dhtThread.getName()) + ' (dht send event thread) could not terminated')

		if (hasattr (self, "mqtt")):
			try:
				self.mqtt.disconnect()
			except Exception, e:
				self.logger.error("Error on stop(): %s" % (e))

		self.logger.info("Ended")

	def getConfiguration(self):
		self.WSUri = ("http://%s:%s/rest/raspberry/" % (self.ipAddress, str(self.port)))

		uri = self.WSUri + "soctemperature"
		socTemperature = {"pin":1,"type": "soctemperature","configuredAs": "Sensor","status": self.getSocTemperature(),"unit":"C","rest":"GET","ws":uri}
		

		humVal, tempVal = self.getDHTValues()
		uri = self.WSUri + "humidity"
		humidity = {"pin":2,"type": "Humidity","configuredAs": "Sensor","status": humVal,"unit":"%","rest":"GET","ws":uri}

		uri = self.WSUri + "temperature"
		temperature = {"pin":3,"type": "Temperature","configuredAs": "Sensor","status": tempVal,"unit":"C","rest":"GET","ws":uri}

		functions = [socTemperature, humidity, temperature]
		result = {"configured": True,"ip": str(self.ipAddress),"subnet": "","gateway": "","port": str(self.port),"description": "raspberry pi","type": "raspberry","isError": False, "functions": functions}

		return json.dumps(result)

	def getSocTemperature(self):
		cmd = '/opt/vc/bin/vcgencmd measure_temp| egrep "[0-9.]{4,}" -o'
		ps = subprocess.Popen(cmd,shell=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
		output = ps.communicate()[0]
		return output.replace('\n', '')

	def makeEvent (self, event, value):
		#payload=('{"event":"SoC Temperature","value":"%s","unit":"°C"}' % (self.getSocTemperature()))
		if event is "motion":
			topic = EventTopics.getBehaviourMotion() + "/" + str(self.ipAddress)
		else:
			topic = EventTopics.getSensorMeasurementEvent() + "/" + str(self.ipAddress) + "/" + event
		timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
		payload = (MQTTPayload.getActuationPayload() %  (str(topic), str(value), str(event), str(self.ipAddress), str(timestamp)))
		return topic, payload


	def GET(self, *ids):
		result = ""
		if len(ids) > 0:
			param_0 = str(ids[0]).lower()
			if param_0 == "configuration":			
				result += self.getConfiguration()
			
			elif param_0 == "soctemperature":	
				socTemp = self.getSocTemperature()
				topic, payload = self.makeEvent("soctemperature", socTemp)
				result += payload

			elif param_0 == "temperature":	
				humVal, tempVal = self.getDHTValues()			
				topic, payload = self.makeEvent("temperature", tempVal)
				result += payload

			elif param_0 == "humidity":
				humVal, tempVal = self.getDHTValues()					
				topic, payload = self.makeEvent("humidity", humVal)
				result += payload
			
			else:
				self.logger.error("Command not found")
				raise cherrypy.HTTPError("404 Not found", "command not found")

		else:
			self.logger.error("Command not found")
			raise cherrypy.HTTPError("404 Not found", "command not found")

	
		return result	

		
	def POST(self, *ids):
		self.logger.error("Subclasses must override POST(self, *ids)!")
		raise NotImplementedError('subclasses must override POST(self, *ids)!')
		
	def PUT(self, *ids):
		self.logger.error("Subclasses must override PUT(self, *ids)!")
		raise NotImplementedError('subclasses must override PUT(self, *ids)!')
		
	def DELETE(self, *ids):
		self.logger.error("Subclasses must override DELETE(self, *ids)!")
		raise NotImplementedError('subclasses must override DELETE(self, *ids)!')
