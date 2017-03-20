#!/usr/bin/python

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



DEFAULT_BROKER_URI = "localhost"
DEFAULT_BROKER_PORT = "1883"

httpPort = 8080
#httpPort = 443
#logLevel = logging.DEBUG
logLevel = logging.INFO

class HomeAgent(AbstractAgentClass):
	exposed = True

	def __init__(self, serviceName, logLevel):
		super(HomeAgent, self).__init__(serviceName, logLevel)

	def getMountPoint(self):
		return '/rest/home'

	def start (self):
		self.copyDefaultFile()
		myhome = json.loads(self.getConfiguration())
		self.brokerUri = myhome["homeMessageBroker"]["address"]
		self.brokerPort = myhome["homeMessageBroker"]["port"]
		if (self.brokerUri is None and self.brokerUri == "") and (self.brokerPort is None and self.brokerPort == ""):
			self.brokerUri = DEFAULT_BROKER_URI
			self.brokerPort = DEFAULT_BROKER_PORT

		self.mqtt = MyMQTTClass(self.serviceName, self.logger, self)
		self.mqtt.connect(self.brokerUri, self.brokerPort)

		self.logger.info("Started")

	def stop(self):
		self.logger.info("Ended")
		sys.exit(0)

	def copyDefaultFile(self):
		newtFile = os.path.join(os.getcwd(), "../conf")
		if not os.path.exists(newtFile):
			try:
				os.makedirs(newtFile)
			except Exception, e:
				self.logger.error("unable to make \"conf\" directory: %s" % (e))

		
		newtFile =  os.path.join(newtFile, "home_structure.json")
		if not os.path.isfile(newtFile):
			try:
				defaultFile = os.path.join(os.getcwd(), "home/home_structure.json")
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
		path = os.path.join(os.getcwd(), "../conf/home_structure.json")
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

				fullpath = os.path.join(os.getcwd(), "../conf/home_structure.json")
				if os.path.exists(fullpath):
					try:
						json_data=open(fullpath).read()
						localHome = json.loads(json_data)

						remoteHomeJson = cherrypy.request.body.read()
						remoteHome = json.loads(remoteHomeJson)

						timestamp = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')
						for localRule in localHome['rules']:
							for remoteRule in remoteHome['rules']:
								if localRule['ruleSID'] == remoteRule['ruleSID']:
									localRule['isRuleEnabled'] = remoteRule['isRuleEnabled'] 
									topic = EventTopics.getRuleEnabler() + "/" + localRule['ruleSID'] 
									payload = (MQTTPayload.getActuationPayload() %  (str(topic), str(localRule['isRuleEnabled']), EventTopics.getRuleEnabler(), localRule['ruleSID'], str(timestamp)))
									self.mqtt.syncPublish(topic, payload, 2)

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


if __name__ == "__main__":
	home = HomeAgent("HomeAgent", logLevel)
	AbstractAgent.startCherrypy(httpPort, home)
	




	
	

	

