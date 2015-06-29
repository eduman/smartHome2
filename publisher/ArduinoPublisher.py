#!/usr/bin/python

import cherrypy
import logging
import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)

from myMqtt import EventTopics
from AbstractPublisher import AbstractPublisher 


	
class ArduinoPublisher(AbstractPublisher):
	exposed = True
	

	def __init__ (self, serviceName, logLevel):
		super (ArduinoPublisher, self).__init__(serviceName, logLevel)

	def GET(self, **params):
		deviceID = measureType = measureValue = None
		if "deviceID" in params.keys(): 
			deviceID = params["deviceID"]
		
		if "measureType" in params.keys(): 
			measureType = params["measureType"]

		if "measureValue" in params.keys(): 
			measureValue = params["measureValue"]
		
		if measureType != None and deviceID != None and measureValue != None:
			if measureType.lower() == "motion":
				if measureValue.lower() == "false" or measureValue.lower() == "true":
					topic = self.makeActionEvent(EventTopics.getBehaviourMotion(), deviceID, "")
					payload = self.publishEvent(topic, deviceID, EventTopics.getBehaviourMotion(), measureValue)
					msg = ("event %s sent with topic %s" % (payload, topic))
				else:
					self.logger.error ("404 Not found: Unaspected value for measureValue")
					raise cherrypy.HTTPError("404 Not found", "Unaspected value for measureValue")
			else:
				self.logger.error ("404 Not found: Unaspected value for measureType")
				raise cherrypy.HTTPError("404 Not found", "Unaspected value for measureType")
		else:
			self.logger.error ("400 Bad Request: Unaspected parameters")
			raise cherrypy.HTTPError("400 Bad Request", "Unaspected parameters ")

		return msg

		
	def POST(self, **params):
		raise cherrypy.HTTPError("501 Not implemented", "Method POST not implemented" )

	def PUT(self, **params):
		raise cherrypy.HTTPError("501 Not implemented", "Method PUT not implemented" )

	def DELETE(self, **params):
		raise cherrypy.HTTPError("501 Not implemented", "Method DELETE not implemented" )



