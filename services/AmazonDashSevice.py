from scapy.all import *
import logging
import datetime
import time
import shutil
import json
import urllib
import urllib2
import httplib
import ssl
import os, sys
import threading
from threading import Thread

from abstractService.AbstractService import AbstractServiceClass


lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import MQTTPayload 
from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass  
from mySSLUtil import MySSLUtil
from myConfigurator import CommonConfigurator


#logLevel = logging.INFO
logLevel = logging.DEBUG

DEFAULT_BROKER_URI = "localhost"
DEFAULT_BROKER_PORT = "1883"

man = '\n--scan\tto find new amazon dash id\n--help\tto show the help\nNo argument are needed to run the service'
argumentsError='\ncommand not valid. Execute with:%s' % (man)



class AmazonDashSevice(AbstractServiceClass):

	def __init__(self, serviceName, logLevel, arguments):
		super(AmazonDashSevice, self).__init__(serviceName, logLevel)
		self.arguments = arguments
		self.checkSudoer()

	def checkSudoer(self):
		user = os.getenv("SUDO_USER")
		if user is None:
			self.logger.error("This program needs 'sudo' presmissions")
			self.stop()		

	def start (self):
		if ("--scan" in sys.argv):
			self.logger.info("Scanning...")
			while (True):
				try:
					print sniff(prn=self.scan_arp_display, filter="arp", store=0, count=0)
				except:
					pass
				time.sleep(0.1)
		elif ("--help" in sys.argv):
			self.logger.info(man)
		elif (len(sys.argv) == 1):
			self.logger.info("%s started" % self.serviceName)
			self.retrieveHomeSettings()
			self.brokerUri = self.myhome["homeMessageBroker"]["address"]
			self.brokerPort = self.myhome["homeMessageBroker"]["port"]
			self.mqtt = MyMQTTClass(self.serviceName, self.logger, self)
			self.mqtt.connect(self.brokerUri, self.brokerPort)
			
			self.homeUpdateThread = Thread (target = self.homeUpdate)
			self.homeUpdateThread.start()
			while (self.isRunning):
				try:
					print sniff(prn=self.arp_display, filter="arp", store=0, count=0)
				except:
					pass
				time.sleep(0.1)
		else: 
			self.logger.error(argumentsError)


	def stop(self): 
		if (hasattr (self, "mqtt")):
			try:
				self.mqtt.disconnect()
			except Exception, e:
				self.logger.error("Error on stop(): %s" % (e))
		super(AmazonDashSevice, self).stop()


	def retrieveHomeSettings(self):
		super(AmazonDashSevice, self).retrieveHomeSettings()
		self.myButtons = {}
		for button in self.myhome["buttons"]:
			self.myButtons[button["buttonID"]] = button


	def arp_display(self, pkt):
		if pkt[ARP].op == 1:
			hwsrc = pkt[ARP].hwsrc
			if (hwsrc in self.myButtons):
				button = self.myButtons[hwsrc]
				if (button["protocol"].upper() == "REST" and button["type"].upper() == "GET"):
					resp, isOk = self.invokeWebService (button["action"])
					if (isOk):
						self.logger.debug("command sent to %s" % (button["action"]))
					else:
						self.logger.error('Unable to send command %s: %s' % (button["action"], resp))
				#TODO, other REST methods must be implemented if needed
				elif (button["protocol"].upper() == "MQTT" and button["type"].upper() == "PUB"):
					timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
					payload = (MQTTPayload.getActuationPayload() %  (button["action"], "toggle", "toggle", button["action"], str(timestamp)))		
					self.mqtt.syncPublish(button["action"], payload, 2)  
				else:
					self.logger.error('The following parameter values are given wrong, protocol: %s; type: %s' % (button["protocol"], button["type"]))

	def scan_arp_display(self, pkt):
		print "ok--"
		hwsrcList = []
		if pkt[ARP].op == 1: #who-has (request)
			if (pkt[ARP].hwsrc not in hwsrcList):
				hwsrcList.append(pkt[ARP].hwsrc)
			self.logger.info(hwsrcList)

		# 	if pkt[ARP].psrc == '0.0.0.0': # ARP Probe
		# 		self.logger.info("ARP Probe from: " + pkt[ARP].hwsrc)


if __name__ == "__main__":
	buttons = AmazonDashSevice("AmazonDashSevice", logLevel, sys.argv)
	buttons.start()
