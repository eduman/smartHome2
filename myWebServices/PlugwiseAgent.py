#!/usr/bin/python
# -*- coding: iso-8859-15 -*-
import cherrypy
import logging
import os
import datetime
import time
import os, sys
import optparse
import logging
from serial.serialutil import SerialException
import threading
import json
import inspect


from plugwise import *
import plugwise.util

import abstractAgent.AbstractAgent as AbstractAgent
from abstractAgent.AbstractAgent import AbstractAgentClass

lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import MQTTPayload 
from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass  
from mySSLUtil import MySSLUtil



httpPort = 8083
#httpPort = 443
#logLevel = logging.DEBUG
logLevel = logging.INFO

DEFAULT_SERIAL_PORT = "/dev/ttyUSB0"

wsBase = "http://%s:%s%s/%s/%s"
jsonMsg = '{"configured":true,"ip":"%s","subnet":"","gateway":"","port":"","description": "plugwise","type": "plugwise","isError":false,"functions":[%s]}'
function = '{"pin":%s,"type":"%s","configuredAs":"%s","status":"%s","unit":"%s","rest": "GET","ws":"%s"}'


class PlugwiseAgent(AbstractAgentClass):
	exposed = True

	def __init__(self, serviceName, logLevel):
		super(PlugwiseAgent, self).__init__(serviceName, logLevel)
		self.__lock = threading.Lock()
		self.myhome = self.retriveHomeSettings()

		

	def getMountPoint(self):
		return '/rest/plugwise'

	def start (self):

		self.serialPort = DEFAULT_SERIAL_PORT
		self.plugwiseAgentID =  (inspect.stack()[0][1]).replace(".py", "").replace("./", "")

		for gateway in self.myhome["plugwiseGateways"]:
			#scanner = self.caseInsensitive(scanner)
			if gateway["plugwiseGatewayID"] == self.plugwiseAgentID:
				self.serialPort = gateway["serialPort"]
				found = True

		if (not found):
			self.logger.error ("PlugwiseAgentID = %s not found in myHome json", self.plugwiseAgentID)
			sys.exit()


		self.appliances = {}
		for room in self.myhome['rooms']:
			for device in room['devices']:
				if device['type'].lower() == "plugwise":
					self.appliances[device['deviceID']] = device['description']

		self.stickSerial = Stick(self.serialPort)
		self.logger.info("Started")

	def stop(self):
		self.logger.info("Ended")
		sys.exit(0)

	def getConfiguration (self, circle, plugwiseID):
		try:
			power = "%.2f" % (circle.get_power_usage())
		except ValueError:
			power = "0.00"

	#GET LAST 4 VALUES OF CURRENT FROM THE CIRCLE BUFFER
	#    for dt, watt_hours in c.get_power_usage_history(None):
	#        if dt is None:
	#            ts_str,watt_hours = "N/A", "N/A"
	#        else:
	#            ts_str = dt.strftime("%Y-%m-%d %H")
	#            watt_hours = "%f" % (watt_hours,)
	#        print("\t%s %s Wh" % (ts_str, watt_hours))


		values = ""
		#status = str(circle.get_info())[144:145]

		toSearch = "\'relay_state\': 1" 
		response = str(circle.get_info())

		if response.find(toSearch) != -1:
			status = "1"
			switchWS = wsBase % (self.getIpAddress(), httpPort, self.getMountPoint(), plugwiseID, "off")
		else:
			status = "0"
			switchWS = wsBase % (self.getIpAddress(), httpPort, self.getMountPoint(), plugwiseID, "on")

		if self.appliances.get(plugwiseID) is not None:
			applianceName = self.appliances.get(plugwiseID)
		else:
			applianceName = "Unknown appliance"


		infoWS= wsBase % (self.getIpAddress(), httpPort, self.getMountPoint(), plugwiseID, "configuration")
		values += function % (1, applianceName, "switch", status, "", switchWS) + ","
		values += function % (2, "Power", "sensor", power, "W", infoWS) 
		result = jsonMsg % (plugwiseID, values)
		return status, result


	def GET(self, *ids):
		self.__lock.acquire()
		result = ""
		status = ""
		error = None
		try: 
			if len(ids) > 1:
				plugwiseID = str(ids[0]).lower() 
				command = str(ids[1]).lower()
				
				circle = Circle(plugwiseID, self.stickSerial)

				if command == "configuration":			
					status, result = self.getConfiguration(circle, plugwiseID)
				
				elif command == "on":
					circle.switch_on()			
					status, result = self.getConfiguration(circle, plugwiseID) 

				elif command == "off":	
					circle.switch_off()			
					status, result = self.getConfiguration(circle, plugwiseID)

				elif command == "toggle":
					status, trash = self.getConfiguration(circle, plugwiseID)
					if (status == "0"):
						circle.switch_on()
					elif (status == "1"):
						circle.switch_off()
					status, result = self.getConfiguration(circle, plugwiseID)
					#devStatus= json.loads(self.getConfiguration(circle, plugwiseID))
					#for function in devStatus["functions"]:
					#	if (function["configuredAs"] == "switch"):
					#		if (function["status"] == "0")
					#			circle.switch_on()
					#		elif (function["status"] == "1")
					#			circle.switch_off()
					#result += self.getConfiguration(circle, plugwiseID)

				
				else:
					self.logger.error("Command not found")
					error = "Command not found"
					#raise cherrypy.HTTPError("404 Not found", "command not found")

			else:
				self.logger.error("Command not found")
				error = "Command not found"
				#raise cherrypy.HTTPError("404 Not found", "command not found")
		except (TimeoutException, SerialException) as reason:
			self.logger.error("Error: %s" % (reason))
			error =  "Error: %s" % (reason)
			#raise cherrypy.HTTPError("404 Not found", ("Error: %s" % (reason)))

		self.__lock.release()

		if error is not None:
			raise cherrypy.HTTPError("404 Not found", ("Error: %s" % (error)))
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

if __name__ == "__main__":
	plugwise = PlugwiseAgent("PlugwiseAgent", logLevel)
	AbstractAgent.startCherrypy(httpPort, plugwise)


