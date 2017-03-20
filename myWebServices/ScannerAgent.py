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
import inspect

import abstractAgent.AbstractAgent as AbstractAgent
from abstractAgent.AbstractAgent import AbstractAgentClass

lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import MQTTPayload 
from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass  
from mySSLUtil import MySSLUtil
from DropboxAgent import DropboxAgent


httpPort = 8085
#httpPort = 443
#logLevel = logging.DEBUG
logLevel = logging.INFO

class ScannerAgent(AbstractAgentClass):
	exposed = True

	def __init__(self, serviceName, logLevel):
		super(ScannerAgent, self).__init__(serviceName, logLevel)
		self.myhome = self.retriveHomeSettings()
		self.scannerid =  (inspect.stack()[0][1]).replace(".py", "").replace("./", "")
		found = False
		for scanner in self.myhome["scanners"]:
			#scanner = self.caseInsensitive(scanner)
			if scanner["scannerID"] == self.scannerid:
				self.imageFolder = scanner["imageFolder"]
				accessToken = self.myhome["DropboxAgent"]["accessToken"]
				remoteFolder = self.myhome["DropboxAgent"]["remoteFolder"]
				dropbox = DropboxAgent ("DropboxAgent", logLevel, accessToken)
				dropbox.start(self.imageFolder, remoteFolder)
				found = True


		if (not found):
			self.logger.error ("ScannerID = %s not found in myHome json", self.scannerid)
			sys.exit()

	def getMountPoint(self):
		return '/rest/scanner'


	def start (self):
		
		if not os.path.exists(self.imageFolder):
			try:
				os.makedirs(self.imageFolder)
			except Exception, e:
				self.logger.error("Error in creating image folder: %s" % (e))

		self.logger.info("Started")
		

	def stop(self):
		self.logger.info("Ended")

	def getConfiguration(self):
		result = ('{"configured": true,"ip": "%s","subnet": "","gateway": "","port":"%s","description": "scanner","type": "scanner","isError": false,"functions": [{"pin":1,"type": "Scan","configuredAs": "Button","status":"","unit":"","rest":"GET","ws":"http://%s:%s%s/scan"}]}' % (self.getIpAddress(), str(httpPort), self.getIpAddress(), str(httpPort), self.getMountPoint()))
		return result

	def scan(self):
		if not os.path.exists(self.imageFolder):
			try:
				os.makedirs(self.imageFolder)
			except Exception, e:
				self.logger.error("Error in creating image folder: %s" % (e))
				raise cherrypy.HTTPError("404 Not found", ("error in creating image folder: %s" % (e)))

		path = os.path.join(os.getcwd(), "scanner/scan.sh")
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

		#return '{"event":"Scanning result","value":"Scanning the image","unit":""}'
		return self.getConfiguration()

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


if __name__ == "__main__":
	scanner = ScannerAgent("ScannerAgent", logLevel)
	AbstractAgent.startCherrypy(httpPort, scanner)


