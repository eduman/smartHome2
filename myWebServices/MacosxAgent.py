#!/usr/bin/python

import cherrypy
import logging
import os
import datetime
import time
import os, sys
import subprocess
import stat

COMMANDS = ["BrightnessDown", "BrightnessUp", "PlayerNext", "PlayerPlayPause", "PlayerPrevious", "PlayerStop", "VolumeDown", "VolumeUp", "VolumeMute"]
URIS = ["configuration"]
for command in COMMANDS:
	if command is not "VolumeMute":
		URIS += [command.lower()]

URIS += ["volumemutedfalse", "volumemutedtrue"]

class MacosxAgent(object):
	exposed = True

	def __init__(self, serviceName, logLevel, ipAddress, port):
		self.serviceName = serviceName
		self.ipAddress = ipAddress
		self.port = port
		logPath = "log/%s.log" % (self.serviceName)
		
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


	def start (self, cherrypyEngine):

		if hasattr(cherrypyEngine, 'signal_handler'):
			cherrypyEngine.signal_handler.subscribe()
		
		cherrypyEngine.subscribe('stop', self.stop())

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

				functions += ('{"pin":%d,"type": "%s","configuredAs": "Button","status":"%s","unit":"","rest":"GET","ws":"http://%s:%s/rest/macosx/%s"}' %(pin, command, muteValue, self.ipAddress, self.port, ws))

			else:
				functions += ('{"pin":%d,"type": "%s","configuredAs": "Button","status":"ok","unit":"","rest":"GET","ws":"http://%s:%s/rest/macosx/%s"}' %(pin, command, self.ipAddress, self.port, command.lower()))

			pin += 1
			if pin < len(COMMANDS):
				functions += ","

		configJson = ('{"configured": true,"ip": "%s","subnet": "","gateway": "","port":"%s","description": "macosx","type": "macosx","isError": false,"functions": [%s]}' % (self.ipAddress, self.port, functions))

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
				result += self.launchCommand("myWebServices/macosx/%s.sh" % param_0.lower())
						
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