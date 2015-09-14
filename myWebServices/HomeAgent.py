#!/usr/bin/python

import cherrypy
import logging
import os
import datetime
import time
import os, sys
import shutil
import json


class HomeAgent(object):
	exposed = True

	def __init__(self, serviceName, logLevel):
		self.serviceName = serviceName
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

		self.copyDefaultFile()
		self.logger.info("Started")

	def stop(self):
		self.logger.info("Ended")

	def copyDefaultFile(self):
		newtFile = os.path.join(os.getcwd(), "conf")
		print newtFile
		if not os.path.exists(newtFile):
			try:
				os.makedirs(newtFile)
			except Exception, e:
				self.logger.error("unable to make \"conf\" directory: %s" % (e))

		
		if not os.path.isfile(newtFile):
			try:
				newtFile =  os.path.join(newtFile, "home_structure.json")
				print newtFile
				defaultFile = os.path.join(os.getcwd(), "myWebServices/home/home_structure.json")
				shutil.copy2(defaultFile, newtFile)
			except Exception, e:
				self.logger.error("error in copying the defalut \"home_structure.json\" file: %s" % (e))
		#try:
		#	defaultFile = os.path.join(os.getcwd(), "myWebServices/home/home_structure.json")
		#	newtFile = os.path.join(os.getcwd(), "conf/home_structure.json")
		#	shutil.copy2(defaultFile, newtFile)
		#except Exception, e:
			#self.logger.error("error in copying the defalut \"home_structure.json\" file: %s" % (e))
			#raise cherrypy.HTTPError("404 Not found", ("error in copying the defalut \"home_structure.json\" file: %s" % (e)))


	def getConfiguration(self):
		result = ""
		path = os.path.join(os.getcwd(), "conf/home_structure.json")
		if os.path.exists(path):
			try:
				with open(path, "r") as myfile:
					result=myfile.read()
			except Exception, e:
				raise cherrypy.HTTPError("404 Not found", ("error in executing the script: %s" % (e)))
		else:
			self.copyDefaultFile()
			result += self.getConfiguration()

		return result

	def GET(self, *ids):
		result = ""
		if len(ids) > 0:
			param_0 = str(ids[0]).lower()
			if param_0 == "configuration":			
				result += self.getConfiguration()
			else:
				self.logger.error("The file \"home_structure.conf\" is not found in \"conf\" directory")
				raise cherrypy.HTTPError("404 Not found", "the file \"home_structure.conf\" is not found in \"conf\" directory")

		else:
			self.logger.error("Command not found")
			raise cherrypy.HTTPError("404 Not found", "command not found")
	
		return result	

		
	def POST(self, *ids):
		self.logger.error("Subclasses must override POST(self, *ids)!")
		raise NotImplementedError('subclasses must override POST(self, *ids)!')
		
	def PUT(self, *ids):
		result = ""
		if len(ids)  > 1:
			param_0 = str(ids[0]).lower()
			param_1 = str(ids[1]).lower()
			if param_0 == "configuration" and param_1 == "updaterule":

				fullpath = os.path.join(os.getcwd(), "conf/home_structure.json")
				if os.path.exists(fullpath):
					try:
						json_data=open(fullpath).read()
						localHome = json.loads(json_data)

						remoteHomeJson = cherrypy.request.body.read()
						remoteHome = json.loads(remoteHomeJson)

						for localRule in localHome['rules']:
							for remoteRule in remoteHome['rules']:
								if localRule['ruleSID'] == remoteRule['ruleSID']:
									localRule['isRuleEnabled'] = remoteRule['isRuleEnabled'] 

						f = open(fullpath,'w')
						string = json.dumps(localHome)
						f.write(string)
						f.close()
					except Exception, e:
						self.logger.error("Unable to update \"home_structure.conf\": %s" % e)
						raise cherrypy.HTTPError("404 Not found", "unable to update \"home_structure.conf\": %s" % e)		

					result += self.getConfiguration()
				else:
					self.logger.error("The file \"home_structure.conf\" is not found in \"conf\" directory")
					raise cherrypy.HTTPError("404 Not found", "the file \"home_structure.conf\" is not found in \"conf\" directory")
			else:
				self.logger.error("Wrongparameters %s. Use /rest/home/configuration/updaterule" % str (ids))
				raise cherrypy.HTTPError("404 Not found", "wrong parameters %s. Use /rest/home/configuration/updaterule" % str (ids))

		else:
			self.logger.error("Wrong parameters %s. Use /rest/home/configuration/updaterule" % str (ids))
			raise cherrypy.HTTPError("404 Not found", "wrong parameters %s. Use /rest/home/configuration/updaterule" % str (ids))
	
		return result	
		
	def DELETE(self, *ids):
		self.logger.error("Subclasses must override DELETE(self, *ids)!")
		raise NotImplementedError('subclasses must override DELETE(self, *ids)!')
