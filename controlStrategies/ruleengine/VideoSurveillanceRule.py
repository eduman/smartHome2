#!/usr/bin/env python

from AbstractActionRule import AbstractActionRule
import time
import ConfigurationConstants
import threading
from Context import Context
import logging
import datetime
#from DefaultTimerRule import DefaultTimerRule
import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import EventTopics
from smartHomeDevice import ActuationCommands
from utitlityLib import Utilities

from subprocess import call
import StringIO
import time
from time import strftime
import os

#IMAGE_FILE_PATH = "/tmp/image.jpg"
#IMAGE_DATETIME_FILE_PATH = "/tmp/image-datetime.jpg"
#RBPI_PHOTO_COMMAND = "raspistill"
RBPI_VIDEO_COMMAND = "raspivid"
#VLC_STREAMING_COMMAND = " | cvlc -vvv stream:///dev/stdin --sout '#standard{access=http,mux=ts,dst=:8090}' :demux=h264"
#VLC_STREAMING_URL = "http://%s:8090/"


class VideoSurveillanceRule(AbstractActionRule):

	def __init__(self, context, logger, mqttServiceProvider):
		super(VideoSurveillanceRule, self).__init__(context, logger, mqttServiceProvider)


	def process(self):
		self.logger.debug("Processing VideoSurveillanceRule...")
		isRuleEnabled = True
		try:
			isRuleEnabled = Utilities.to_bool(self.context.getProperty(ConfigurationConstants.getIsRuleEnabled()))
		except ValueError, e:
			self.logger.error("Error on VideoSurveillanceRule.process(): %s. Setting True as default value for isRuleEnabled" % (e))
		
		try:
			self.isLooked = Utilities.to_bool(self.context.getProperty(ConfigurationConstants.getIsLooked()))
		except ValueError, e:
			self.logger.error("Error on VideoSurveillanceRule.process(): %s. Setting False as default value for isLooked " % (e))
			self.isLooked = False


		if isRuleEnabled:
			if (self.isLooked):
				self.startWebCam()
			else:		
				self.stopWebCam()

			self.context.updateProperty(ConfigurationConstants.getIsLooked(), self.isLooked)

	def stop(self):
		self.stopWebCam()


	def startWebCam(self):
		try:
			command_vlc = "ps aux | grep -v grep | grep cvlc"
			ret = call (command_vlc, shell=True)
			if (ret != 0):
				command = "raspivid  -o - --timeout 9999999 --rotation 270 --height 240 --width 320   | cvlc -vvv stream:///dev/stdin --sout '#standard{access=http,mux=ts,dst=:8090}' :demux=h264"
				streaming_th  = threading.Thread(target = self.launch_cmd, args=[command])
				streaming_th.setDaemon(True)
				streaming_th.start()
				self.logger.info("VideoSurveillanceRule.process(): starting Videocam streaming")

			else:
				self.logger.debug("Warning on VideoSurveillanceRule.process(): Videocam is already streaming")


			
		except Exception, e:	
			self.logger.error("Error on VideoSurveillanceRule.process(): %s" % (e))


	def stopWebCam(self):
		cmds = {'vlc',RBPI_VIDEO_COMMAND}
		self.logger.info("VideoSurveillanceRule.process(): stopping Videocam streaming")

		for cmd in cmds:
			self.logger.debug("killing %s" % cmd)
			kill_command = "ps aux | grep " + cmd + " | grep -v grep | tr -s ' ' | cut -d' ' -f2 | tr -s '\n' ' ' | sed -e s/^/kill\ -9\ /g | bash"
			call (kill_command, shell=True)


	def launch_cmd (self, command):
		self.logger.debug("launching %s" % command)
		code = call (command, shell=True)


	


