
import json
import time
import requests
import logging
import os
import sys
import signal
import threading
from threading import Thread


import urllib
import urllib2
import httplib
import requests
import miniupnpc
import ipaddress

lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import EventTopics
from myConfigurator import CommonConfigurator  

requests.packages.urllib3.disable_warnings()


class AbstractServiceClass(object):

	def __init__ (self, serviceName, logLevel):
		self.serviceName = serviceName
		self.homeUpdateTimer = 300 # update every 5 minutes
		self.isRunning = True 

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

		for sig in (signal.SIGABRT, signal.SIGILL, signal.SIGINT, signal.SIGSEGV, signal.SIGTERM):
			signal.signal(sig, self.signal_handler)


	def signal_handler(self, signal, frame):
		self.stop()

	def retrieveHomeSettings(self):
		resp, isOk = self.invokeWebService(self.homeWSUri)
		while (not isOk):
			self.logger.error ("Unable to find the home proxy. I will try again in a while...")
			resp, isOk = self.invokeWebService(self.homeWSUri)
			time.sleep(10) #sleep 10 seconds
		self.myhome = json.loads(resp)


	def invokeWebService (self, uri):
		try:
			req = urllib2.Request(uri)
			req.add_header('Content-Type', 'application/json')
			resp = urllib2.urlopen(req).read()
			return resp, True

		except urllib2.HTTPError, e:
			msg = 'HTTPError: %s.' % e
			self.logger.error(msg)
			return msg, False
		except urllib2.URLError, e:
			msg = 'URLError: %s.' % e
			self.logger.error(msg)
			return msg, False
		except httplib.HTTPException, e:
			msg = 'HTTPException: %s.' % e
			self.logger.error(msg)
			return msg, False
		except Exception, e:
			msg = 'generic exception: %s.' % e
			self.logger.error(msg)
			return msg, False


	def homeUpdate(self):
		while (self.isRunning):
			time.sleep(self.homeUpdateTimer)
			self.retrieveHomeSettings()

	def start(self):
		raise NotImplementedError('subclasses must override start(self)!')

	def stop(self):
		self.isRunning = False
		if (hasattr(self, "homeUpdateThread")):
			if self.homeUpdateThread.isAlive():
				try:
					self.homeUpdateThread._Thread__stop()
				except:
					self.logger.error(str(self.homeUpdateThread.getName()) + ' (update home thread) could not be terminated')
				self.logger.info("Ended")
		sys.exit(0)