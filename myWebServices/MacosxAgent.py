#!/usr/bin/python

import cherrypy
import logging
import os
import datetime
import time
import os, sys
import subprocess
import stat

import abstractAgent.AbstractAgent as AbstractAgent
from abstractAgent.AbstractAgent import AbstractAgentClass

lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import MQTTPayload 
from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass  
from mySSLUtil import MySSLUtil


httpPort = 8082
#httpPort = 443
#logLevel = logging.DEBUG
logLevel = logging.INFO

COMMANDS = ["BrightnessDown", "BrightnessUp", "PlayerNext", "PlayerPlayPause", "PlayerPrevious", "PlayerStop", "VolumeDown", "VolumeUp", "VolumeMute"]
URIS = ["configuration"]
for command in COMMANDS:
	if command is not "VolumeMute":
		URIS += [command.lower()]

URIS += ["volumemutedfalse", "volumemutedtrue"]

class MacosxAgent(AbstractAgentClass):
	exposed = True

	def __init__(self, serviceName, logLevel):
		super(MacosxAgent, self).__init__(serviceName, logLevel)

	def getMountPoint(self):
		return '/rest/macosx'


	def start (self):
		self.logger.info("Started")

	def stop(self):
		self.logger.info("Ended")


	def getConfiguration(self):
		functions = ""
		pin = 0
		for command in COMMANDS:
			if command == COMMANDS[8]:
				# managing VolumeMute
				cmd = "osascript -e 'output muted of (get volume settings)'"
				ps = subprocess.Popen(cmd,shell=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
				output = ps.communicate()[0]
				if "true" in output.lower():
					muteValue = "Muted"
					ws = "volumemutedfalse"
				else:
					muteValue = "ToBeMuted"
					ws = "volumemutedtrue"

				functions += ('{"pin":%d,"type": "%s","configuredAs": "Button","status":"%s","unit":"","rest":"GET","ws":"http://%s:%s/rest/macosx/%s"}' %(pin, command, muteValue, self.getIpAddress(), httpPort, ws))

			else:
				functions += ('{"pin":%d,"type": "%s","configuredAs": "Button","status":"ok","unit":"","rest":"GET","ws":"http://%s:%s/rest/macosx/%s"}' %(pin, command, self.getIpAddress(), httpPort, command.lower()))

			pin += 1
			if pin < len(COMMANDS):
				functions += ","

		configJson = ('{"configured": true,"ip": "%s","subnet": "","gateway": "","port":"%s","description": "macosx","type": "macosx","isError": false,"functions": [%s]}' % (self.getIpAddress(), httpPort, functions))

		return configJson


	def launchCommand(self, script):
		result = ""
		path = os.path.join(os.getcwd(), script)
		if os.path.exists(path):
			try:
				st = os.stat(path)
				os.chmod(path, st.st_mode | stat.S_IEXEC)
				subprocess.check_output(path)
				result += self.getConfiguration()
			except Exception, e:
				self.logger.error("Error in executing the script: %s" % (e))
				raise cherrypy.HTTPError("404 Not found", ("error in executing the script: %s" % (e)))
		else:
			self.logger.error("Script not found")
			raise cherrypy.HTTPError("404 Not found", "script not found")

		return result

	def GET(self, *ids):
		result = ""
		if len(ids) > 0:
			param_0 = str(ids[0]).lower()
			if param_0 ==  URIS[0]:	
				result += self.getConfiguration()

			elif param_0 in URIS and param_0 is not URIS[0]:
				result += self.launchCommand("macosx/%s.sh" % param_0.lower())
						
			else:
				self.logger.error("command not found")
				raise cherrypy.HTTPError("404 Not found", "command not found")

		else:
			self.logger.error("command not found")
			raise cherrypy.HTTPError("404 Not found", "command not found")

	
		return result	

		
	def POST(self, *ids):
		self.logger.error("Must override POST(self, *ids)!")
		raise NotImplementedError('subclasses must override POST(self, *ids)!')
		
	def PUT(self, *ids):
		self.logger.error("Must override PUT(self, *ids)!")
		raise NotImplementedError('subclasses must override PUT(self, *ids)!')
		
	def DELETE(self, *ids):
		self.logger.error("Must override DELETE(self, *ids)!")
		raise NotImplementedError('subclasses must override DELETE(self, *ids)!')



if __name__ == "__main__":
	macosx = MacosxAgent("MacosxAgent", logLevel)
	AbstractAgent.startCherrypy(httpPort, macosx)

