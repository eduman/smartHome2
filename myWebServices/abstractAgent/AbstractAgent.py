#!/usr/bin/python

import cherrypy
import logging
import datetime
import time
import shutil
import json
import urllib
import urllib2
import httplib
import ssl
import socket
import os, sys


lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import MQTTPayload 
from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass  
from mySSLUtil import MySSLUtil
from myConfigurator import CommonConfigurator


def startCherrypy(httpPort, agent):
	agent.start()
	cherrypy.config.update({'server.socket_host': '0.0.0.0'})
	cherrypy.config.update({'server.socket_port': httpPort})

	# Uncomment the following line to enable ssl 
	#cherrypy.config.update({'server.ssl_module':'pyopenssl'})
	#cherrypy.config.update({'server.ssl_certificate':'/Users/edo/Documents/Git/smartHome2/keys/cert.pem'})
	#cherrypy.config.update({'server.ssl_private_key':'/Users/edo/Documents/Git/smartHome2/keys/key.pem'})

	#logging files
	#cherrypy.config.update({'log.error_file':'log/Web.log'})
	#cherrypy.config.update({'log.access_file' : 'log/Access.log'})

	#does not print tracebacks on web page
	cherrypy.config.update({'request.show_tracebacks': True})
	#disable cherrypy console log 
	cherrypy.config.update({'log.screen': False})
	
	# activate signal listening
	if hasattr(cherrypy.engine, 'signal_handler'):
		cherrypy.engine.signal_handler.subscribe()

	cherrypy.tree.mount(agent, agent.getMountPoint() , agent.getCherrypyConfig())
	#start serving pages
	cherrypy.engine.start()
	cherrypy.engine.start()
	cherrypy.engine.block()


class AbstractAgentClass(object):
	def __init__(self, serviceName, logLevel):
		self.serviceName = serviceName
		logPath = "../log/%s.log" % (self.serviceName)
		
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

		self.commonConfigPath = "../conf/microservice.conf"
		try:
			self.homeWSUri = CommonConfigurator.getHomeEndPointValue(self.commonConfigPath)
		except Exception, e:
			self.logger.error('Unable to start %s due to: %s' % (self.serviceName, e))
			self.stop()


	def getIpAddress (self):
		ipaddress = "127.0.0.1"
		try:
			ipaddress = ([(s.connect(('8.8.8.8', 80)), s.getsockname()[0], s.close()) for s in [socket.socket(socket.AF_INET, socket.SOCK_DGRAM)]][0][1])
		except Exception, e:
			pass
		return ipaddress
	def caseInsensitive(self, jsonDict):
		return dict(map(lambda (key, value):(key.lower(),value), jsonDict.items()))

	def retriveHomeSettings(self):
		resp, isOk = self.invokeWebService(self.homeWSUri)
		while (not isOk):
			self.logger.error ("Unable to find the home proxy. I will try again in a while...")
			resp, isOk = self.invokeWebService(self.homeWSUri)
			time.sleep(10) #sleep 10 seconds
		myhome = json.loads(resp)
		return myhome

	def invokeWebService (self, uri):
		try:
			req = urllib2.Request(uri)
			req.add_header('Content-Type', 'application/json')
			resp = urllib2.urlopen(req).read()
			# Uncomment the following line to enable ssl 
			#resp = urllib2.urlopen(req, context=MySSLUtil.makeDefaultSSLContext()).read()
			return resp, True

		except urllib2.HTTPError, e:
		    return ('HTTPError = %s.' % e ), False
		except urllib2.URLError, e:
		    return ('URLError = %s.' % e ), False
		except httplib.HTTPException, e:
		    return ('HTTPException = %s.' % e), False
		except Exception, e:
			return ('generic exception: %s.' % e ), False

	def getCherrypyConfig (self):
		conf = {
	        '/': {
	            'request.dispatch': cherrypy.dispatch.MethodDispatcher()
	        }
	    }
		return conf

	def getMountPoint(self):
		raise NotImplementedError('subclasses must override getMountPoint(self)!')

	def start(self, cherrypyEngine):
		raise NotImplementedError('subclasses must override start(self, cherrypyEngine)!')

	def stop(self, cherrypyEngine):
		raise NotImplementedError('subclasses must override stop(self)!')