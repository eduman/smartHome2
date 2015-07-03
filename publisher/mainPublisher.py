#!/usr/bin/python

import cherrypy
import logging
import urllib
import urllib2
import logging
import httplib
import json
import time

import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)

from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass

import ArduinoPublisher

httpPort = 8084
ArduinoServiceName = "ArduinoPublisher"
logLevel = logging.DEBUG



def start():
	homeWSUri = "http://localhost:8080/rest/home/configuration"
	#homeWSUri = "http://192.168.1.5:8080/rest/home/configuration"

	logger = makeLogger("mainPublisher")

	conf = {
	        '/': {
	            'request.dispatch': cherrypy.dispatch.MethodDispatcher()
	        }
	    }

	cherrypy.config.update({'server.socket_host': '0.0.0.0'})
	cherrypy.config.update({'server.socket_port': httpPort})
	#does not print tracebacks on web page
	cherrypy.config.update({'request.show_tracebacks': False})
	#disable cherrypy console log 
	cherrypy.config.update({'log.screen': False})
	
	# activate signal listening
	if hasattr(cherrypy.engine, 'signal_handler'):
		cherrypy.engine.signal_handler.subscribe()


	resp, isOk = invokeWebService(homeWSUri)
	while (not isOk):
		logger.error ("Unable to find the home proxy. I will try again in a while...")
		resp, isOk = invokeWebService(homeWSUri)
		time.sleep(10) #sleep 10 seconds
	myhome = json.loads(resp)

	brokerUri = myhome["homeMessageBroker"]["address"]
	brokerPort = myhome["homeMessageBroker"]["port"]
	if (brokerUri != None and brokerUri != "") and (brokerPort != None and brokerPort != ""):
		arduinoPublisher = ArduinoPublisher.ArduinoPublisher(ArduinoServiceName, logLevel)
		arduinoPublisher.start(cherrypy.engine, brokerUri, brokerPort)
		cherrypy.tree.mount(arduinoPublisher, '/ArduinoPublisher', conf)
	else:
		logger.error ("The message broker address is not valid. Arduino publisher is not running...")



	

    #start serving pages
	cherrypy.engine.start()
	cherrypy.engine.block()


def invokeWebService (uri):
	try:
		req = urllib2.Request(uri)
		req.add_header('Content-Type', 'application/json')
		resp = urllib2.urlopen(req).read()
		return resp, True

	except urllib2.HTTPError, e:
	    return ('HTTPError = %s.' % e ), False
	except urllib2.URLError, e:
	    return ('URLError = %s.' % e ), False
	except httplib.HTTPException, e:
	    return ('HTTPException = %s.' % e), False
	except Exception, e:
		return ('generic exception: %s.' % e ), False

def makeLogger(serviceName):
	logPath = "log/%s.log" % (serviceName)
	if not os.path.exists(logPath):
		try:
			os.makedirs(os.path.dirname(logPath))
		except Exception, e:
			pass	

	logger = logging.getLogger(serviceName)
	logger.setLevel(logLevel)
	hdlr = logging.FileHandler(logPath)
	formatter = logging.Formatter(serviceName + ": " + "%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
	hdlr.setFormatter(formatter)
	logger.addHandler(hdlr)
	
	consoleHandler = logging.StreamHandler()
	consoleHandler.setFormatter(formatter)
	logger.addHandler(consoleHandler)
	return logger


if __name__ == "__main__":
	start()
