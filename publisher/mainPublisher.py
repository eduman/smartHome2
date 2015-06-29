#!/usr/bin/python

import cherrypy
import logging
import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)

from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass

import ArduinoPublisher

httpPort = 8084
ArduinoServiceName = "ArduinoPublisher"
logLevel = logging.DEBUG

DEFAULT_BROKER_URI = "192.168.1.5"
#DEFAULT_BROKER_PORT = "1883"


def start():


	conf = {
	        '/': {
	            'request.dispatch': cherrypy.dispatch.MethodDispatcher()
	        }
	    }

	cherrypy.config.update({'server.socket_host': '0.0.0.0'})
	cherrypy.config.update({'server.socket_port': httpPort})
	#does not print tracebacks on the web page
	cherrypy.config.update({'request.show_tracebacks': False})
	#disable the cherrypy console log 
	cherrypy.config.update({'log.screen': False})
	
	# activate signal listening
	if hasattr(cherrypy.engine, 'signal_handler'):
		cherrypy.engine.signal_handler.subscribe()

	arduinoPublisher = ArduinoPublisher.ArduinoPublisher(ArduinoServiceName, logLevel)
	#arduinoPublisher.start(cherrypy.engine, DEFAULT_BROKER_URI, DEFAULT_BROKER_PORT)
	arduinoPublisher.start(cherrypy.engine, DEFAULT_BROKER_URI)
	cherrypy.tree.mount(arduinoPublisher, '/ArduinoPublisher', conf)

    #start serving pages
	cherrypy.engine.start()
	cherrypy.engine.block()



if __name__ == "__main__":
	start()
