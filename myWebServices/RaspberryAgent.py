#!/usr/bin/python
# -*- coding: iso-8859-15 -*-
import cherrypy
import logging
import os
import datetime
import time
import os, sys
import subprocess



class RaspberryAgent(object):
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



	def start (self, cherrypyEngine):

		if hasattr(cherrypyEngine, 'signal_handler'):
			cherrypyEngine.signal_handler.subscribe()
		
		cherrypyEngine.subscribe('stop', self.stop())

		self.logger.info("Started")

	def stop(self):
		self.logger.info("Ended")

	def getConfiguration(self):
		temp = self.getTemperaure()
		result = ('{"configured": true,"ip": "%s","subnet": "","gateway": "","port":"%s","description": "raspberry pi","type": "raspberry","isError": false,"functions": [{"pin":1,"type": "SoC Temperature","configuredAs": "Sensor","status":"%s","unit":"°C","rest":"GET","ws":"http://%s:%s/rest/raspberry/temperature"}]}' % (self.ipAddress, str(self.port), temp, self.ipAddress, str(self.port)))

		return result

	def getTemperaure(self):
		cmd = '/opt/vc/bin/vcgencmd measure_temp| egrep "[0-9.]{4,}" -o'
		ps = subprocess.Popen(cmd,shell=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
		output = ps.communicate()[0]
		return output

	def getTemperatureEvent (self):
		result=('{"event":"SoC Temperature","value":"%s","unit":"°C"}' % (self.getTemperaure()))
		return result


	def GET(self, *ids):

		result = ""
		if len(ids) > 0:
			param_0 = str(ids[0]).lower()
			if param_0 == "configuration":			
				result += self.getConfiguration()
			
			elif param_0 == "temperature":				
				result += self.getTemperatureEvent()
			
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