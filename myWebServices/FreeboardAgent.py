#!/usr/bin/python
# -*- coding: iso-8859-15 -*-

import cherrypy
import logging
import os
import datetime
import time
import os, sys
import shutil
import json


import abstractAgent.AbstractAgent as AbstractAgent
from abstractAgent.AbstractAgent import AbstractAgentClass

lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import MQTTPayload 
from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass  
from mySSLUtil import MySSLUtil




FREEBOARD_ROOT = 'static/freeboard/'
httpPort = 8081
#httpPort = 443
#logLevel = logging.DEBUG
logLevel = logging.INFO


class FreeboardAgent(AbstractAgentClass):
	exposed = True

	def __init__(self, serviceName, logLevel):
		super(FreeboardAgent, self).__init__(serviceName, logLevel)

	def getCherrypyConfig (self):
		path = os.path.abspath(os.path.dirname(__file__))
		self.freeboard = os.path.join(path, FREEBOARD_ROOT)
		dashboard = os.path.join(self.freeboard, 'dashboard')
		self.dashboardJsonPath = os.path.join(dashboard, 'dashboard.json')
		freeboard_conf = {
				'/': {
		            'request.dispatch': cherrypy.dispatch.MethodDispatcher()
		        },
				'/static/js':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': os.path.join(self.freeboard, 'js')
		        },'/static/css':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': os.path.join(self.freeboard, 'css')
		        },'/static/dashboard':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': dashboard
		        },'/static/img':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': os.path.join(self.freeboard, 'img')
		        },'/static/plugins/freeboard':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': os.path.join(self.freeboard, 'plugins/freeboard')
		        },'/static/plugins/thirdparty':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': os.path.join(self.freeboard, 'plugins/thirdparty')
		        },'/static/plugins/mqtt':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': os.path.join(self.freeboard, 'plugins/mqtt')
		        },'/static/plugins/onlinux':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': os.path.join(self.freeboard, 'plugins/onlinux')
		        }

		}
		return freeboard_conf

	def getMountPoint(self):
		return "/"


	def start (self):
		self.logger.info("Started")

	def stop(self):
		self.logger.info("Ended")
		sys.exit(0)

	def GET(self, *ids):
		if len(ids) > 1:
			param_0 = str(ids[0]).lower()
			param_1 = str(ids[1])
			if param_0 == "static" and param_1 is not None:	
				try:
					resurce = open(os.path.join(self.freeboard, param_1))

				except:
					self.logger.error("Resource not found")
					raise cherrypy.HTTPError("404 Not found", "resource not found")
			else:
				self.logger.error("Resource not found")
				raise cherrypy.HTTPError("404 Not found", "resource not found")
		else:
			# by default it returns the index html
			resurce = open(os.path.join(self.freeboard, "index.html"))
			

		return resurce
	
	def POST(self, *ids, **params):
		if len(ids) > 1:
			param_0 = str(ids[0]).lower()
			param_1 = str(ids[1])
			if param_0 == "static" and param_1 == "saveDashboard":	
				try:
					with open(self.dashboardJsonPath, 'w') as outfile:
						outfile.write(params['json_string'].encode('utf-8'))
						outfile.close()
				except Exception, e:
					self.logger.error("Unable to save: %s" % e)
					raise cherrypy.HTTPError("404 Not found", "Unable to save: %s" % e)
			else:
				self.logger.error("Resource not found")
				raise cherrypy.HTTPError("404 Not found", "resource not found")
		return open(os.path.join(self.freeboard, "index.html"))
		
	def PUT(self, *ids):
		self.logger.error("Must override PUT(self, *ids)!")
		raise NotImplementedError('subclasses must override PUT(self, *ids)!')
		
	def DELETE(self, *ids):
		self.logger.error("Must override DELETE(self, *ids)!")
		raise NotImplementedError('subclasses must override DELETE(self, *ids)!')

if __name__ == "__main__":
	freeboard = FreeboardAgent("FreeboardAgent", logLevel)
	AbstractAgent.startCherrypy(httpPort, freeboard)
	