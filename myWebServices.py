#!/usr/bin/python
# -*- coding: iso-8859-15 -*-

import cherrypy
import logging
import urllib
import urllib2
import logging
import httplib
import json
import time
import socket
import ConfigParser
import os, sys
#lib_path = os.path.abspath(os.path.join('..', 'commons'))
#sys.path.append(lib_path)

from commons.myMqtt import EventTopics
from commons.myMqtt.MQTTClient import MyMQTTClass

from publisher.ArduinoPublisher import ArduinoPublisher
from myWebServices.UserPresenceManager import UserPresenceManager
from myWebServices.MacosxAgent import MacosxAgent
from myWebServices.SwitchOffAllDevicesAgent import SwitchOffAllDevicesAgent
from myWebServices.HomeAgent import HomeAgent
from myWebServices.ScannerAgent import ScannerAgent
#from myWebServices.PlugwiseAgent import PlugwiseAgent
import myWebServices.WebServicesConfigurationConstants as WSConstants
from myWebServices.FreeboardAgent import FreeboardAgent
from myWebServices.DropboxAgent import DropboxAgent

httpPort = 8080
logLevel = logging.DEBUG

FREEBOARD_ROOT = 'myWebServices/static/freeboard/'



def start():
	logger = makeLogger("myWebServices")
	configPath = os.path.join(os.getcwd(), "conf/myWebService.conf")
	config = ConfigParser.SafeConfigParser()

	try: 
		if not os.path.exists(configPath):
			makeDefaultConfigFile(configPath)

		config.read(configPath)
	except Exception, e:
		logger.error ("Error on myWebService.start(): %s" % (e))
		raise Exception("Error on myWebService.start(): %s" % (e))


	
	conf = {
	        '/': {
	            'request.dispatch': cherrypy.dispatch.MethodDispatcher()
	        }
	    }


	
	cherrypy.config.update({'server.socket_host': '0.0.0.0'})
	cherrypy.config.update({'server.socket_port': httpPort})
	#does not print tracebacks on web page
	cherrypy.config.update({'request.show_tracebacks': True})
	#disable cherrypy console log 
	cherrypy.config.update({'log.screen': False})
	
	# activate signal listening
	if hasattr(cherrypy.engine, 'signal_handler'):
		cherrypy.engine.signal_handler.subscribe()


	ipAddress = ([(s.connect(('8.8.8.8', 80)), s.getsockname()[0], s.close()) for s in [socket.socket(socket.AF_INET, socket.SOCK_DGRAM)]][0][1])



	if config.getboolean(WSConstants.getAgentsSettings(), WSConstants.getFreeboardAgent()):
		path = os.path.abspath(os.path.dirname(__file__))
		freeboard = os.path.join(path, FREEBOARD_ROOT)
		dashboard = os.path.join(freeboard, 'dashboard')
		dashboardJsonPath = os.path.join(dashboard, 'dashboard.json')
		freeboard_conf = {
				'/': {
		            'request.dispatch': cherrypy.dispatch.MethodDispatcher()
		        },
				'/static/js':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': os.path.join(freeboard, 'js')
		        },'/static/css':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': os.path.join(freeboard, 'css')
		        },'/static/dashboard':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': dashboard
		        },'/static/img':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': os.path.join(freeboard, 'img')
		        },'/static/plugins/freeboard':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': os.path.join(freeboard, 'plugins/freeboard')
		        },'/static/plugins/thirdparty':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': os.path.join(freeboard, 'plugins/thirdparty')
		        },'/static/plugins/mqtt':{
		        'tools.staticdir.on': True,
		        'tools.staticdir.dir': os.path.join(freeboard, 'plugins/mqtt')
		        }

		}

		freeboardAgent = FreeboardAgent("FreeboardAgent", logLevel, FREEBOARD_ROOT, dashboardJsonPath)
		freeboardAgent.start(cherrypy.engine)
		cherrypy.tree.mount(freeboardAgent, '/', freeboard_conf)

	if config.getboolean(WSConstants.getAgentsSettings(), WSConstants.getHomeAgent()):
		home = HomeAgent("HomeAgent", logLevel)
		home.start(cherrypy.engine)
		cherrypy.tree.mount(home, '/rest/home', conf)


	if config.getboolean(WSConstants.getAgentsSettings(), WSConstants.getScannerAgent()):
		imageFolder = config.get(WSConstants.getScannerAgentSettings(), WSConstants.getScannerFolder())
		#imageFolder = '/home/pi/smartHome2/scanner/images'
		scanner = ScannerAgent("ScannerAgent", logLevel, ipAddress, httpPort)
		scanner.start(cherrypy.engine, imageFolder)
		cherrypy.tree.mount(scanner, '/rest/scanner', conf)


	if config.getboolean(WSConstants.getAgentsSettings(), WSConstants.getDropboxAgent()):
		# it get the same localFolder for the scanner
		localFolder = config.get(WSConstants.getScannerAgentSettings(), WSConstants.getScannerFolder())
		# run DropboxAgent (python DropboxAgent.py) to retrieve your userID and accessToken
		accessToken = config.get(WSConstants.getDropboxAgentSettings(), WSConstants.getDropboxAccessToken())
		userID = config.get(WSConstants.getDropboxAgentSettings(), WSConstants.getDropboxUserID())
		remoteFolder = config.get(WSConstants.getDropboxAgentSettings(), WSConstants.getDropboxRemoteFolder())
		dropbox = DropboxAgent ("DropboxAgent", logLevel, userID, accessToken)
		dropbox.start(localFolder, remoteFolder)
		

	if config.getboolean(WSConstants.getAgentsSettings(), WSConstants.getMacosxAgent()):
		macosx = MacosxAgent("MacosxAgent", logLevel, ipAddress, httpPort)
		macosx.start(cherrypy.engine)
		cherrypy.tree.mount(macosx, '/rest/macosx', conf)


	#start serving pages
	cherrypy.engine.start()


	homeWSUri = config.get(WSConstants.getAgentsSettings(), WSConstants.getHomeURI())
	resp, isOk = invokeWebService(homeWSUri)
	while (not isOk):
		logger.error ("Unable to find the home proxy. I will try again in a while...")
		resp, isOk = invokeWebService(homeWSUri)
		time.sleep(10) #sleep 10 seconds
	myhome = json.loads(resp)

	brokerUri = myhome["homeMessageBroker"]["address"]
	brokerPort = myhome["homeMessageBroker"]["port"]
	if (brokerUri != None and brokerUri != "") and (brokerPort != None and brokerPort != ""):
		if config.getboolean(WSConstants.getAgentsSettings(), WSConstants.getArduinoPublisher()):
			arduinoPublisher = ArduinoPublisher("ArduinoPublisher", logLevel)
			arduinoPublisher.start(cherrypy.engine, brokerUri, brokerPort)
			cherrypy.tree.mount(arduinoPublisher, '/rest/arduino/publisher', conf)

		if config.getboolean(WSConstants.getAgentsSettings(), WSConstants.getUserPresenceManager()):
			upm = UserPresenceManager("UserPresenceManager", logLevel, myhome)
			upm.start(cherrypy.engine, brokerUri, brokerPort)
			cherrypy.tree.mount(upm, '/rest/userpresence', conf)

		if config.getboolean(WSConstants.getAgentsSettings(), WSConstants.getSwitchOffAllDevicesAgent()): 
			switchOff = SwitchOffAllDevicesAgent ("SwitchOffAllDevicesAgent", logLevel)
			switchOff.start(cherrypy.engine, brokerUri, brokerPort)
			cherrypy.tree.mount(switchOff, '/rest/switchoffall', conf)

	   	if config.getboolean(WSConstants.getAgentsSettings(), WSConstants.getRaspberryAgent()) and (os.uname()[4].startswith("arm")):
                	from myWebServices.RaspberryAgent import RaspberryAgent
                	raspberry = RaspberryAgent("RasberryAgent", logLevel, ipAddress, httpPort, 900)
                	raspberry.setDHTInstalled(True)
	                raspberry.setDHTPin(18)
	                raspberry.setDHTType(22)
	                raspberry.setPirInstalled(True)
	                raspberry.setPirPin(7)
	                raspberry.start(cherrypy.engine, brokerUri, brokerPort)
	                cherrypy.tree.mount(raspberry, '/rest/raspberry', conf)

	else:
		logger.error ("The message broker address is not valid. Web Services are not running...")	

	if config.getboolean(WSConstants.getAgentsSettings(), WSConstants.getPlugwiseAgent()):
		from myWebServices.PlugwiseAgent import PlugwiseAgent
		plugwiseSerialPort = config.get(WSConstants.getPlugwiseAgentSettings(), WSConstants.getPlugwiseSerialPort())
		#plugwiseSerialPort = "/dev/ttyUSB0"
		appliances = {}
		for room in myhome['rooms']:
			for device in room['devices']:
				if device['type'].lower() == "plugwise":
					appliances[device['deviceID']] = device['description']
		plugwise = PlugwiseAgent("PlugwiseAgent", logLevel, ipAddress, httpPort)
		plugwise.start(cherrypy.engine, appliances, plugwiseSerialPort)
		cherrypy.tree.mount(plugwise, '/rest/plugwise', conf)


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


def makeDefaultConfigFile(configPath):
	try:
		os.makedirs(os.path.dirname(configPath))
	except Exception, e:
		pass

	f = open(configPath, "w+")
	section = WSConstants.getAgentsSettings()

	
	ConfigParser.SafeConfigParser.add_comment = lambda self, section, option, value: self.set(section, '\n; '+option, value)
	config = ConfigParser.SafeConfigParser()
	config.add_section(section)

	config.add_comment(section, "Enable your agents eg." + WSConstants.getHomeAgent(), "True")
	for key in WSConstants.getAgents():
		config.set(section, key, "True")
	
	config.set(section, WSConstants.getHomeURI(), "http://localhost:8080/rest/home/configuration")
	

	section = WSConstants.getScannerAgentSettings()
	config.add_section(section)
	config.set(section, WSConstants.getScannerFolder(), "/home/pi/smartHome2/scanner/images")

	section = WSConstants.getDropboxAgentSettings()
	config.add_section(section)
	config.set(section, WSConstants.getDropboxRemoteFolder(), "/scanner")
	config.set(section, WSConstants.getDropboxUserID(), WSConstants.getDefaultDropboxUserID())
	config.set(section, WSConstants.getDropboxAccessToken(), WSConstants.getDefaultDropboxAccessToken())
	
	section = WSConstants.getPlugwiseAgentSettings()
	config.add_section(section)
	config.set(section, WSConstants.getPlugwiseSerialPort(),  "/dev/ttyUSB0")

	config.write(f)


if __name__ == "__main__":
	start()
