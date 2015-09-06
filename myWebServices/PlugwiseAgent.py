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

from plugwise import *
import plugwise.util

DEFAULT_SERIAL_PORT = "/dev/ttyUSB0"

wsBase = "http://%s:%s/rest/plugwise/%s/%s"
jsonMsg = '{"configured":true,"ip":"%s","subnet":"","gateway":"","port":"","description": "plugwise","type": "plugwise","isError":false,"functions":[%s]}'
function = '{"pin":%s,"type":"%s","configuredAs":"%s","status":"%s","unit":"%s","rest": "GET","ws":"%s"}'


class PlugwiseAgent(object):
	exposed = True

	def __init__(self, serviceName, logLevel, ipAddress, port):
		self.serviceName = serviceName
		self.ipAddress = ipAddress
		self.port = port
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

	def start (self, cherrypyEngine, appliances, serialPort = DEFAULT_SERIAL_PORT):

		if hasattr(cherrypyEngine, 'signal_handler'):
			cherrypyEngine.signal_handler.subscribe()
		
		cherrypyEngine.subscribe('stop', self.stop())

		self.serialPort = serialPort

		self.stickSerial = Stick(self.serialPort)

		self.appliances = {}
		if appliances is not None:
			self.appliances = appliances

		self.logger.info("Started")

	def stop(self):
		self.logger.info("Ended")

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
			switchWS = wsBase % (self.ipAddress, self.port, plugwiseID, "off")
		else:
			status = "0"
			switchWS = wsBase % (self.ipAddress, self.port, plugwiseID, "on")

		if self.appliances.get(plugwiseID) is not None:
			applianceName = self.appliances.get(plugwiseID)
		else:
			applianceName = "Unknown appliance"


		infoWS= wsBase % (self.ipAddress, self.port, plugwiseID, "configuration")
		values += function % (1, applianceName, "switch", status, "", switchWS) + ","
		values += function % (2, "Power", "sensor", power, "W", infoWS) 
		result = jsonMsg % (plugwiseID, values)
		return result


	def GET(self, *ids):

		result = ""
		try: 
			if len(ids) > 1:
				plugwiseID = str(ids[0]).lower() 
				command = str(ids[1]).lower()
				
				circle = Circle(plugwiseID, self.stickSerial)

				if command == "configuration":			
					result += self.getConfiguration(circle, plugwiseID)
				
				elif command == "on":
					circle.switch_on()			
					result += self.getConfiguration(circle, plugwiseID) 

				elif command == "off":	
					circle.switch_off()			
					result += self.getConfiguration(circle, plugwiseID) 
				
				else:
					self.logger.error("Command not found")
					raise cherrypy.HTTPError("404 Not found", "command not found")

			else:
				self.logger.error("Command not found")
				raise cherrypy.HTTPError("404 Not found", "command not found")
		except (TimeoutException, SerialException) as reason:
			self.logger.error("Error: %s" % (reason)) 
			raise cherrypy.HTTPError("404 Not found", ("Error: %s" % (reason)))

	
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
