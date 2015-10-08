#!/usr/bin/python

import cherrypy
import logging
import os
import datetime
import time
import os, sys
import shutil
import json


FREEBOARD_ROOT = 'myWebServices/static/freeboard/'

class FreeboardAgent(object):
	exposed = True

	def __init__(self, serviceName, logLevel, freeboardRoot, dashboardJsonPath):
		self.serviceName = serviceName
		logPath = "log/%s.log" % (self.serviceName)
		self.freeboardRoot = freeboardRoot
		self.dashboardJsonPath = dashboardJsonPath
		
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

	def GET(self, *ids):
		if len(ids) > 1:
			param_0 = str(ids[0]).lower()
			param_1 = str(ids[1])
			if param_0 == "static" and param_1 is not None:	
				try:
					resurce = open(os.path.join(self.freeboardRoot, param_1))
				except:
					self.logger.error("Resource not found")
					raise cherrypy.HTTPError("404 Not found", "resource not found")
			else:
				self.logger.error("Resource not found")
				raise cherrypy.HTTPError("404 Not found", "resource not found")
		else:
			# by default it returns the index html
			resurce = open(os.path.join(self.freeboardRoot, "index.html"))
			

		return resurce
	
	def POST(self, *ids, **params):
		if len(ids) > 1:
			param_0 = str(ids[0]).lower()
			param_1 = str(ids[1])
			if param_0 == "static" and param_1 == "saveDashboard":	
				try:
					with open(self.dashboardJsonPath, 'w') as outfile:
						#print params['json_string']
						outfile.write(params['json_string'])
						outfile.close()
				except Exception, e:
					self.logger.error("Unable to save: %s" % e)
					raise cherrypy.HTTPError("404 Not found", "Unable to save: %s" % e)
			else:
				self.logger.error("Resource not found")
				raise cherrypy.HTTPError("404 Not found", "resource not found")
		return open(os.path.join(self.freeboardRoot, "index.html"))
		
	def PUT(self, *ids):
		self.logger.error("Must override PUT(self, *ids)!")
		raise NotImplementedError('subclasses must override PUT(self, *ids)!')
		
	def DELETE(self, *ids):
		self.logger.error("Must override DELETE(self, *ids)!")
		raise NotImplementedError('subclasses must override DELETE(self, *ids)!')