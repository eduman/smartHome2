#!/usr/bin/env python

from abstractControlStategy.AbstractControlStategy import AbstractControlStategy
from ruleengine.Context import Context
from ruleengine.RuleEngine import RuleEngine
from ruleengine.RuleUpdater import RuleUpdater
from ruleengine.InititializationRule import InititializationRule
from ruleengine.LoadRuleConfig import LoadRuleConfig
from ruleengine.SaveStatusRule import SaveStatusRule
from ruleengine import ConfigurationConstants

import os, sys
lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass
from smartHomeDevice import ActuationCommands

from ruleengine.VideoSurveillanceRule import VideoSurveillanceRule

import logging
import os
import signal
import sys
import inspect
import json


homeWSUri = "http://localhost:8080/rest/home/configuration"
#ruleSID = "VideSurveillanceControlStrategy:UnknownOwner:Strategy"

#homeWSUri = "http://192.168.1.5:8080/rest/home/configuration"
ruleSID = "VideSurveillanceControlStrategy:eduman:ingresso"

#logLevel = logging.INFO
logLevel = logging.DEBUG

def createVideoSurveillanceContext (id):
	ctx = Context(id)
	#default values
	ctx.updateProperty(ConfigurationConstants.getIsLooked(), getDefaultIsLooked())
	return ctx


def getDefaultIsLooked():
	return "False"


class VideoSurveillanceControlStrategy(AbstractControlStategy):
	
	def __init__(self):
		strategyName =  (inspect.stack()[0][1]).replace(".py", "").replace("./", "") #os.path.realpath(__file__) 
		super(VideoSurveillanceControlStrategy, self).__init__(strategyName, logLevel)
		try:

			if os.path.isfile(self.pidfile):
				self.logger.error ("Control strategy \"%s\" is already running with pid %s, exiting" % (self.strategyName, self.pidfile))
				sys.exit()

			else:		

				file(self.pidfile, 'w').write(self.pid)

				for sig in (signal.SIGABRT, signal.SIGILL, signal.SIGINT, signal.SIGSEGV, signal.SIGTERM):
					signal.signal(sig, self.signal_handler)

				self.context = createVideoSurveillanceContext(self.strategyName)
				self.ruleEngine = RuleEngine(self.context, self.logger)
				self.ruleUpdater = RuleUpdater(self.ruleEngine, self.logger)
				
				self.setRuleEngine()
				self.mqtt.subscribeEvent(self.context.getProperty(ConfigurationConstants.getFullActuatorList()), EventTopics.getLookAction())
				self.mqtt.subscribeEvent("alldevices/" + ActuationCommands.getLook(), EventTopics.getLookAction())
				self.loop()
		except KeyboardInterrupt, e:
			self.exit()



	def setRuleEngine(self):
		initRule = InititializationRule(self.context, self.logger)
		self.ruleEngine.addRule(initRule)

		loadRule = LoadRuleConfig(self.context, self.logger, self.configPath, homeWSUri, ruleSID)
		#self.ruleEngine.addRule(loadRule)
		loadRule.process()

		# Now the mqtt broker is known. Connecting before going ahead 
		brokerUri, port = self.context.getProperty(ConfigurationConstants.getMessageBroker()).split(":")
		self.mqtt = MyMQTTClass(self.strategyName, self.logger, self)
		self.mqtt.connect(brokerUri, port)

		videoSurveillanceRule = VideoSurveillanceRule(self.context, self.logger, self.mqtt)
		self.ruleEngine.addRule(videoSurveillanceRule)

		saveRule = SaveStatusRule(self.context, self.logger, self.configPath)
		self.ruleEngine.addRule(saveRule);

		self.ruleEngine.update()
		self.ruleUpdater.start()
	
	def notifyJsonEvent(self, topic, jsonEventString):
		self.logger.debug ("received topic: \"%s\" with msg: \"%s\"" % (topic, jsonEventString))
		data = json.loads(jsonEventString)
		if (data["event"].lower() == ActuationCommands.getLook()):	
			self.ruleEngine.updateProperty(ConfigurationConstants.getIsLooked(), data["value"])


if __name__ == "__main__":
	t = VideoSurveillanceControlStrategy()
