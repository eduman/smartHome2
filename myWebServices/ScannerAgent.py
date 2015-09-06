#!/usr/bin/python
# -*- coding: iso-8859-15 -*-
import cherrypy
import logging
import os
import datetime
import time
import os, sys
import subprocess
import stat



class ScannerAgent(object):
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



	def start (self, cherrypyEngine, imageFolder):

		if hasattr(cherrypyEngine, 'signal_handler'):
			cherrypyEngine.signal_handler.subscribe()
		
		cherrypyEngine.subscribe('stop', self.stop())

		self.imageFolder = imageFolder
		if not os.path.exists(self.imageFolder):
			try:
				os.makedirs(self.imageFolder)
			except Exception, e:
				self.logger.error("Error in creating image folder: %s" % (e))

		self.logger.info("Started")
		

	def stop(self):
		self.logger.info("Ended")

	def getConfiguration(self):
		result = ('{"configured": true,"ip": "%s","subnet": "","gateway": "","port":"%s","description": "scanner","type": "scanner","isError": false,"functions": [{"pin":1,"type": "Scan","configuredAs": "Button","status":"","unit":"","rest":"GET","ws":"http://%s:%s/rest/scanner/scan"}]}' % (self.ipAddress, str(self.port), self.ipAddress, str(self.port)))
		return result

	def scan(self):
		if not os.path.exists(self.imageFolder):
			try:
				os.makedirs(self.imageFolder)
			except Exception, e:
				self.logger.error("Error in creating image folder: %s" % (e))
				raise cherrypy.HTTPError("404 Not found", ("error in creating image folder: %s" % (e)))

		path = os.path.join(os.getcwd(), "myWebServices/scanner/scan.sh")
		if os.path.exists(path):
			try:
				st = os.stat(path)
				os.chmod(path, st.st_mode | stat.S_IEXEC)
				subprocess.call([path, self.imageFolder])
			except Exception, e:
				self.logger.error("Error in executing the script: %s" % (e))
				raise cherrypy.HTTPError("404 Not found", ("error in executing the script: %s" % (e)))
		else:
			self.logger.error("Script not found")
			raise cherrypy.HTTPError("404 Not found", "script not found")

		return '{"event":"Scanning result","value":"Scanning the image","unit":""}'


	def GET(self, *ids):

		result = ""
		if len(ids) > 0:
			param_0 = str(ids[0]).lower()
			if param_0 == "configuration":			
				result += self.getConfiguration()
			
			elif param_0 == "scan":				
				result += self.scan()
			
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