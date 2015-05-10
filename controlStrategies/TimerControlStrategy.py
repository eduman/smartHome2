#!/usr/bin/env python

from abstractControlStategy.AbstractControlStategy import AbstractControlStategy
from ruleengine.Context import Context
from ruleengine.RuleEngine import RuleEngine
from ruleengine.RuleUpdater import RuleUpdater
from ruleengine.InititializationRule import InititializationRule
from ruleengine.DefaultTimerRule import DefaultTimerRule
from ruleengine.RestoreStatusRule import RestoreStatusRule
from ruleengine.DelayTimerActionRule import DelayTimerActionRule
from ruleengine.SaveStatusRule import SaveStatusRule
from ruleengine import ConfigurationConstants
from myMqtt import EventTopics
from myMqtt.MQTTClient import MyMQTTClass
import ruleengine.ConfigurationConstants
import logging
import os
import signal
import sys
import inspect
import json




def createTimerContext (id):
	ctx = Context(id)

	#default values
	ctx.updateProperty(ConfigurationConstants.getMotion(), getDefaultMotionValue())
	ctx.updateProperty(ConfigurationConstants.getPresence(), getDefaultPresenceValue())
	ctx.updateProperty(ConfigurationConstants.getPresenceTimer(), getDefaultDelayTimerPresence())
	ctx.updateProperty(ConfigurationConstants.getAbsenceTimer(), getDefaultDelayTimerAbsence())
			
	return ctx

def getDefaultMotionValue():
	return "False"

def getDefaultPresenceValue():
	return "False"

def getDefaultDelayTimerPresence():
	return "10"

def getDefaultDelayTimerAbsence():
	return "60"


#logLevel = logging.INFO
logLevel = logging.DEBUG

class TimerControlStrategy(AbstractControlStategy):
	
	def __init__(self):
		strategyName =  (inspect.stack()[0][1]).replace(".py", "").replace("./", "") #os.path.realpath(__file__) 
		super(TimerControlStrategy, self).__init__(strategyName, logLevel)
		try:

			if os.path.isfile(self.pidfile):
				self.logger.error ("Control strategy \"%s\" is already running with pid %s, exiting" % (self.strategyName, self.pidfile))
				sys.exit()

			else:		

				file(self.pidfile, 'w').write(self.pid)

				for sig in (signal.SIGABRT, signal.SIGILL, signal.SIGINT, signal.SIGSEGV, signal.SIGTERM):
					signal.signal(sig, self.signal_handler)

				self.context = createTimerContext(self.strategyName)
				self.ruleEngine = RuleEngine(self.context, self.logger)
				self.ruleUpdater = RuleUpdater(self.ruleEngine, self.logger)
				
				self.setRuleEngine()

				self.mqtt.subscribeEvent(self.context.getProperty(ConfigurationConstants.getFullUserList()), EventTopics.getBehaviourProximity())
				self.mqtt.subscribeEvent(self.context.getProperty(ConfigurationConstants.getFullSensorList()), EventTopics.getBehaviourMotion())
				#self.mqtt.subscribeEvent(self.context.getProperty(ConfigurationConstants.getFullButtonList()), EventTopics.getBehaviourButtonPushed())
				
				self.loop()
		except KeyboardInterrupt, e:
			self.exit()


	def setRuleEngine(self):
		initRule = InititializationRule(self.context, self.logger)
		self.ruleEngine.addRule(initRule)

		defaultRule =  DefaultTimerRule(self.context, self.logger, self.configPath)
		self.ruleEngine.addRule(defaultRule)
		defaultRule.process()

		restoreRule = RestoreStatusRule(self.context, self.logger, self.configPath)
		self.ruleEngine.addRule(restoreRule)

		# Now the mqtt broker is known. Connecting before going ahead 
		brokerUri, port = self.context.getProperty(ConfigurationConstants.getMessageBroker()).split(":")
		self.mqtt = MyMQTTClass(self.strategyName, self.logger, self)
		self.mqtt.connect(brokerUri, port)

		delayTimer = DelayTimerActionRule(self.context, self.logger, self.mqtt)
		self.ruleEngine.addRule(delayTimer)

		saveRule = SaveStatusRule(self.context, self.logger, self.configPath)
		self.ruleEngine.addRule(saveRule);

		self.ruleEngine.update()
		self.ruleUpdater.start()
	
	def notifyJsonEvent(self, topic, jsonEventString):
		self.logger.debug ("received topic: \"%s\" with msg: \"%s\"" % (topic, jsonEventString))
		data = json.loads(jsonEventString)
		self.ruleEngine.updateProperty(data["device"], data["value"])


if __name__ == "__main__":
	t = TimerControlStrategy()
