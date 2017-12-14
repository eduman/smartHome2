# dependencies: enum34, twx.botapi, ipaddress and miniupnpc
# sudo pip install enum34
# sudo pip install -i https://testpypi.python.org/pypi twx.botapi
# sudo pip install ipaddress
# sudo pip install miniupnpc

from twx.botapi import TelegramBot 
from twx.botapi import ReplyKeyboardMarkup, ReplyKeyboardHide
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

from abstractService.AbstractService import AbstractServiceClass


lib_path = os.path.abspath(os.path.join('..', 'commons'))
sys.path.append(lib_path)
from myMqtt import EventTopics
from myConfigurator import CommonConfigurator  


logLevel = logging.INFO

class SmartHomeBot(AbstractServiceClass):

	def __init__ (self, serviceName, logLevel):
		super(SmartHomeBot, self).__init__(serviceName, logLevel)
		self.lastUpdateID = 0
		self.validUsers = []
		self.keyboards = {	#"start": [["Rooms" , "All Devices"], ["Switch off all devices", "Rules"]], 
							"start": [["Rooms" , "All Devices"], ["Switch off all devices"]], 
							"Rooms": [], 
							"All Devices": [] , 
							"Rules": []}
		self.keybordForUsers = {}
		self.allDevicesList = {}
		self.alldevicesFunctionsList = {}
		self.allRoomsList = {}
		
		self.upnp = miniupnpc.UPnP()
		self.upnp.discoverdelay = 200
		self.minUPnPPort = 1024
		self.maxUPnPPort = self.minUPnPPort + 20

		

	def stop(self):
		super(SmartHomeBot, self).stop()

	# Starts the communication with the bot
	def start(self):
		self.logger.info("%s started" % self.serviceName)
		self.retrieveHomeSettings()

		if (self.botToken is None):
			self.logger.error ("The Telegram Token is not valid")
			self.stop()
		else:
			self.bot = TelegramBot(self.botToken)
			self.homeUpdateThread = Thread (target = self.homeUpdate)
			self.homeUpdateThread.start()

			# serving clients
			try:
				while self.isRunning:
					try:
					#The get_updates method returns the earliest 100 unconfirmed updates
						updates = self.bot.get_updates(offset = self.lastUpdateID + 1).wait()
						if (updates is not None):
							cont1=len(updates)
							if cont1 != 0:
								replyThread = Thread (target = self.reply, args=[updates[-1]])
								replyThread.start()
								self.lastUpdateID = updates[-1].update_id

						time.sleep(1)
					except Exception, e:
						self.logger.error('Something wrong occurred in telegram communication: %s. restoring the bot' % (e))
						self.bot = TelegramBot(self.botToken)
						
			except KeyboardInterrupt:
				self.stop()


	def makeKeyboards(self):
		# Rooms keyboard
		keyboard = []
		keyoardRow = []
		self.allRoomsList = {}
		for count, room in enumerate(self.myhome["rooms"]):
			if ((count+1) % 3 != 0):
				label = "%s - %s" % (room["roomID"], room["description"])
				keyoardRow.append (label)
				self.allRoomsList[label] = room
			else:
				label = "%s - %s" % (room["roomID"], room["description"])
				keyoardRow.append (label)
				keyboard.append(keyoardRow)
				keyoardRow = []
				self.allRoomsList[label] = room

		toBeAppend = False		
		if (len(keyboard[-1:]) % 3 != 0 ):
			keyoardRow.append ("Back")
			toBeAppend = True
		else:
			keyoardRow.append ("Back")
			keyboard.append(keyoardRow)
			keyoardRow = []
			toBeAppend = False

		if (len(keyboard[-1:]) % 3 != 0 ):
			keyoardRow.append ("Start")
			toBeAppend = True
		else:
			keyoardRow.append ("Start")
			keyboard.append(keyoardRow)
			keyoardRow = []
			toBeAppend = False

		if (toBeAppend):
			keyboard.append(keyoardRow)


		self.keyboards["Rooms"] = keyboard

		# allDevices keyboard
		keyboard = []
		keyoardRow = []
		self.allDevicesList = {}
		for i, room in enumerate(self.myhome["rooms"]):
			for count, device in  enumerate(room["devices"]):
				label = "%s (%s)" % (device["description"], device["deviceID"])
				self.allDevicesList[label] = device
				keyoardRow.append (label)
				keyboard.append(keyoardRow)
				keyoardRow = []
		keyboard.sort()
		keyoardRow = ["Back", "Start"]
		keyboard.append(keyoardRow)
		self.keyboards["All Devices"] = keyboard

		# Rules keyboard
		keyboard = []
		keyoardRow = []
		for count, rule in enumerate(self.myhome["rules"]):
			keyoardRow.append (rule["ruleSID"])
			keyboard.append(keyoardRow)
			keyoardRow = []

		keyoardRow = ["Back", "Start"]
		keyboard.append(keyoardRow)
		self.keyboards["Rules"] = keyboard

	def retrieveDeviceInfo(self, deviceID):
		replyMsg = ("This is %s\n" % (deviceID))
		if (deviceID.lower().startswith("http://") or deviceID.lower().startswith("https://")):
			deviceUri = deviceID
		else:
			deviceUri = self.allDevicesList[deviceID]["protocol"][0]["uri"]

			if (len(self.allDevicesList[deviceID]["thingspeakChannels"]) > 0):
				replyMsg += ("Thingspeak Channels:\n")
				for tsfeed in self.allDevicesList[deviceID]["thingspeakChannels"]:
					replyMsg += ("%s %s\n\n" % (tsfeed["measureType"], tsfeed["readFeed"]))

		resp, isOk = self.invokeWebService (deviceUri)
		reply_markup = None
		keyboard = []
		if (isOk):
			deviceInfo = json.loads (resp)
			for function in deviceInfo["functions"]:
				if (function["configuredAs"].lower() == "switch"):
					if (function["status"] == "0"):
						status = "off"
						nextStatus = "switch on"
					elif (function["status"] == "1"):
						status = "on"
						nextStatus = "switch off"
					else:
						status = "unknown"
					replyMsg += "%s is %s\n" % (function["type"], status)
					label = "%s %s (%s)" % (nextStatus, function["type"], deviceInfo["ip"])
					self.alldevicesFunctionsList[label] = function["ws"]
					keyboard.append([label])

				elif (function["configuredAs"].lower() == "button"):
					replyMsg += "%s\n" % (function["type"])
					label = "%s (%s)" % (function["type"], deviceInfo["ip"])
					self.alldevicesFunctionsList[label] = function["ws"]
					keyboard.append([label])

				elif (function["configuredAs"].lower() == "sensor"):
					replyMsg += "%s is %s%s\n" % (function["type"], function["status"], function["unit"])

			if (len(keyboard)>0):
				keyboard.append(["Back", "Start"])
				#reply_markup = ReplyKeyboardMarkup.create(keyboard, one_time_keyboard= False, selective=True)

		else:
			replyMsg += 'Devices %s is unreachable: %s' % (deviceID, resp)

		return replyMsg, keyboard


	def openPort(self, localIP, localPort, externalPort, protocol):
		ndevices = self.upnp.discover()
		igd = self.upnp.selectigd()
		msg = ""
		errormsg = "Error: host and/or router port must be an integer between %d and %d" % (self.minUPnPPort, self.maxUPnPPort)
		isOk = True

		if (protocol.upper() == 'TCP'):
			protocol = 'TCP'
		elif (protocol.upper() == 'UDP'):
			protocol = 'UDP'
		else:
			protocol = 'TCP'
			msg += "Protocol not valid, using TCP by default\n"

		if (localIP is None):
			localIP = self.upnp.lanaddr
			isOk = isOk and True
		else:
			try:
				#ip = ipaddress.ip_address(unicode(localIP, 'utf8'))
				ip = ipaddress.ip_address(localIP)
				isOk = isOk and True
			except Exception, e:
				msg += str(e) + "\n"
				isOk = isOk and False

		try:
			localPort = int(localPort)
			externalPort = int(externalPort)
			if (localPort > 1 and localPort < 65536):
				isOk = isOk and True
			else:
				isOk = isOk and False
				msg += errormsg + "\n"

			if (externalPort > self.minUPnPPort and externalPort < self.maxUPnPPort):
				isOk = isOk and True
			else:
				isOk = isOk and False
				msg += errormsg + "\n"

		except Exception, e:
			isOk = isOk and False
			msg += errormsg + "\n"


		if isOk:
			# find a free port for the redirection
			available = self.upnp.getspecificportmapping(externalPort, protocol)
			while available != None and externalPort < self.maxUPnPPort :
				externalPort = externalPort + 1
				available = self.upnp.getspecificportmapping(externalPort, protocol)

			if (available is None):
				portMaps = self.upnp.addportmapping(externalPort, protocol, localIP, localPort, 'Open port %u' % externalPort, '')
				if portMaps:
					msg += 'Port forwarding Successed. Now waiting for some request on %s:%u\n' % (self.upnp.externalipaddress() ,externalPort)
				else:
					msg += "Port forwarding failed\n"
			else:
				msg += "Unable to enable port forwarding, no more external ports are available"				

		return msg

	def closePort(self, externalPort, protocol):
		ndevice = self.upnp.discover()
		igd = self.upnp.selectigd()
		msg = ""
		isOk = True

		if (protocol.upper() == 'TCP'):
			protocol = 'TCP'
		elif (protocol.upper() == 'UDP'):
			protocol = 'UDP'
		else:
			protocol = 'TCP'
			msg += "Protocol not valid, using TCP by default\n"

		if (str(externalPort).lower() == "all"):
			for port in range (self.minUPnPPort, self.maxUPnPPort):
				for protocol in ['TCP', 'UDP']:
					available = self.upnp.getspecificportmapping(port, protocol)
					if (available != None):
						portMaps = self.upnp.deleteportmapping(port, protocol)
						if portMaps:
							msg += 'Successfully deleted port mapping for port %s and protocol %s\n' % (port, protocol)
						else:
							msg +=  'Failed to remove port mapping for port %s and protocol %s\n' % (port, protocol)
		else:
			try:
				externalPort = int(externalPort)

				if (externalPort > self.minUPnPPort and externalPort < self.maxUPnPPort):
					isOk = isOk and True
				else:
					isOk = isOk and False
					msg += "Error: router port must be an integer between %d and %d\n" % (self.minUPnPPort, self.maxUPnPPort) 

			except Exception, e:
				isOk = isOk and False
				msg +=  "Error: router port must be an integer between %d and %d\n" % (self.minUPnPPort, self.maxUPnPPort) 

			if (isOk):
				available = self.upnp.getspecificportmapping(externalPort, protocol)
				if (available != None):
					portMaps = self.upnp.deleteportmapping(externalPort, protocol)
					if portMaps:
						msg += 'Successfully deleted port mapping for port %s and protocol %s\n' % (externalPort, protocol)
					else:
						msg +=  'Failed to remove port mapping for port %s and protocol %s\n' % (externalPort, protocol)
				else:
					msg += 'Port mapping does not existing for  port %s and protocol %s\n' % (externalPort, protocol)

		return msg

	def reply (self, update):
		chat_id=update.message.chat.id
		name = update.message.chat.first_name
		replyMsg = ""
		if chat_id in self.validUsers:  
			text = update.message.text
			reply_markup = None

			if (text.lower() == "/start"):
				replyMsg += "Hi %s! Welcome on SmartHome2!" % (name) 
				reply_markup =ReplyKeyboardMarkup.create(self.keyboards["start"], one_time_keyboard= False, selective=True)
				self.keybordForUsers[chat_id] = [self.keyboards["start"]]
				
			elif (text == "Start"):
				replyMsg = "command received"
				reply_markup = ReplyKeyboardMarkup.create(self.keyboards["start"], one_time_keyboard= False, selective=True)
				self.keybordForUsers[chat_id] = [self.keyboards["start"]]

			elif (text.lower().startswith("/openport") ):
				params = text.lower().split(" ")
				params = filter(None, params)
				protocol = "TCP"
				localIP = None
				localPort = None
				externalPort = None
				wrongParamsMsg = "Wrong parameters! Usage:\n/openport -s <localhost port> -d <router port>\n/openport -s <localhost port> -d <router port> -p <TCP or UDP>\n/openport -h <host ip> -s <host port> -d <router port>\n/openport -h <host ip> -s <host port> -d <router port> -p <TCP or UDP>\n" 
				if (len(params) != 5 and len(params) != 7 and len(params) != 9):
					replyMsg = wrongParamsMsg
				elif (len(params) == 5):
					if (params[1].lower() == "-s" and params[3].lower() == "-d"):
						localPort = params[2]
						externalPort = params[4] 
						replyMsg = self.openPort(localIP, localPort, externalPort, protocol)
					else:
						replyMsg = wrongParamsMsg
				elif (len(params) == 7):
					if (params[1].lower() == "-h" and params[3].lower() == "-s" and params[5].lower() == "-d"):
						localIP = params[2]
						localPort = params[4]
						externalPort = params[6] 
						replyMsg = self.openPort(localIP, localPort, externalPort, protocol)
					elif (params[1].lower() == "-s" and params[3].lower() == "-d" and params[5].lower() == "-p"):
						localPort = params[2]
						externalPort = params[4]
						protocol = params[6] 
						replyMsg = self.openPort(localIP, localPort, externalPort, protocol)
					else:
						replyMsg = wrongParamsMsg
				elif (len(params) == 9):
					if (params[1].lower() == "-h" and params[3].lower() == "-s" and params[5].lower() == "-d" and params[7].lower() == "-p"):
						localIP = params[2]
						localPort = params[4]
						externalPort = params[6] 
						protocol = params[8]
						replyMsg = self.openPort(localIP, localPort, externalPort, protocol)
					else:
						replyMsg =  wrongParamsMsg
				else:
					replyMsg = wrongParamsMsg
				
			elif (text.lower().startswith("/closeport") ):
				params = text.lower().split(" ")
				params = filter(None, params)
				protocol = "TCP"
				externalPort = None
				wrongParamsMsg = "Wrong parameters! Usage:\n/closeport -d <router port>\n/closeport -d all\n/closeport -d <router port> -p <TCP or UDP>\n" 
				if (len(params) != 3 and len(params) != 5):
					replyMsg = wrongParamsMsg
				elif (len(params) == 3):
					if (params[1].lower() == "-d"):
						externalPort = params[2]
						replyMsg = self.closePort(externalPort, protocol)
					else:
						replyMsg = wrongParamsMsg
				elif (len(params) == 5):
					if (params[1].lower() == "-d" and params[3].lower() == "-p"):
						externalPort = params[2]
						protocol = params[4] 
						replyMsg = self.closePort(externalPort, protocol)
					else:
						replyMsg = wrongParamsMsg
				

			elif (text.lower().startswith("/allports") ):
				replyMsg = ""
				ndevices = self.upnp.discover()
				igd = self.upnp.selectigd()
				isNoMapping = True

				for port in range (self.minUPnPPort, self.maxUPnPPort):
					available = self.upnp.getspecificportmapping(port, 'TCP')
					if (available != None):
						replyMsg += "port: %d, protocol: TCP\n" % (port)
						isNoMapping = False

					available = self.upnp.getspecificportmapping(port, 'UDP')
					if (available != None):
						replyMsg += "port: %d, protocol: UDP\n" % (port)
						isNoMapping = False


				if (isNoMapping):
					replyMsg += "There is any port mapping active"

			elif (text == "/update"):
				resp, isOk = self.invokeWebService(self.homeWSUri)
				if (isOk):
					self.myhome = json.loads(resp)
					self.botToken = self.myhome["TelegramBot"]["telegramToken"]
					self.validUsers = self.myhome["TelegramBot"]["allowedUserID"]	
					self.makeKeyboards()
					replyMsg = "Bot Updated"
				else:
					replyMsg = "Unable to update the bot: %s" % (resp)

			elif (text.lower() == "/help"):
				replyMsg += "Start a section:\n/start\n"
				replyMsg += "\nCreate port forwarding:\n/openport -s <localhost port> -d <router port>\n/openport -s <localhost port> -d <router port> -p <TCP or UDP>\n/openport -h <host ip> -s <host port> -d <router port>\n/openport -h <host ip> -s <host port> -d <router port> -p <TCP or UDP>\n" 
				replyMsg += "\nClose port forwarding:\n/closeport -d <router port>\n/closeport -d all\n/closeport -d <router port> -p <TCP or UDP>\n" 
				replyMsg += "\nShow all forwarded ports:\n/allports\n"
				replyMsg += "\nUpdate bot engine:\n/update\n"

			elif (text == "Back"):
				if (chat_id in self.keybordForUsers):
					del self.keybordForUsers[chat_id][-1]
					replyMsg = "command received"
					backKeyboard = self.keybordForUsers[chat_id][-1]
				else:
					replyMsg = "Restoring keyboard due to an internal error"
					backKeyboard= self.keyboards["start"]
					self.keybordForUsers[chat_id] = [backKeyboard]
				
				reply_markup = ReplyKeyboardMarkup.create(backKeyboard, one_time_keyboard= False, selective=True)
				
				
				

			elif (text == self.keyboards["start"][0][0]):
				# Rooms
				try:
					replyMsg = "command received"
					self.keybordForUsers[chat_id].append(self.keyboards[self.keyboards["start"][0][0]])
					reply_markup = ReplyKeyboardMarkup.create(self.keyboards[self.keyboards["start"][0][0]], one_time_keyboard= False, selective=True)
				except:
					replyMsg = "Restoring keyboard due to an internal error"
					self.keybordForUsers[chat_id] = [self.keyboards["start"]]
					reply_markup = ReplyKeyboardMarkup.create(self.keyboards["start"], one_time_keyboard= False, selective=True)
				
			elif (text == self.keyboards["start"][0][1]):
				# Devices
				try:
					replyMsg = "command received"
					self.keybordForUsers[chat_id].append(self.keyboards[self.keyboards["start"][0][1]])
					reply_markup = ReplyKeyboardMarkup.create(self.keyboards[self.keyboards["start"][0][1]], one_time_keyboard= False, selective=True)
				except:
					replyMsg = "Restoring keyboard due to an internal error"
					self.keybordForUsers[chat_id] = [self.keyboards["start"]]
					reply_markup = ReplyKeyboardMarkup.create(self.keyboards["start"], one_time_keyboard= False, selective=True)
				

			elif (text == self.keyboards["start"][1][0]):
				# Switch off all devices
				replyMsg = "command received. The execution may take some time. I will keep you updated."
				self.bot.send_message(chat_id, replyMsg , reply_markup=reply_markup)
				resp, isOk = self.invokeWebService (self.myhome["switchOffAllDevicesAgent"])
				if (isOk):
					replyMsg = "command sent to devices"
				else:
					replyMsg = 'Unable to send "switch off" command to devices: %s' % (resp)
			
#			elif (text == self.keyboards["start"][1][1]):
#				# Rules
#				try:
#					replyMsg = "command received"
#					self.keybordForUsers[chat_id].append(self.keyboards[self.keyboards["start"][1][1]])
#					reply_markup = ReplyKeyboardMarkup.create(self.keyboards[self.keyboards["start"][1][1]], one_time_keyboard= False, selective=True)
#				except:
#					replyMsg = "Restoring keyboard due to an internal error"
#					self.keybordForUsers[chat_id] = [self.keyboards["start"]]
#					reply_markup = ReplyKeyboardMarkup.create(self.keyboards["start"], one_time_keyboard= False, selective=True)


			elif (text in self.allRoomsList):
				# Devices per room keyboard
				keyboard = []
				keyoardRow = []
				for count, device in  enumerate(self.allRoomsList[text]["devices"]):
					label = "%s (%s)" % (device["description"], device["deviceID"])
					self.allDevicesList[label] = device
					keyoardRow.append (label)
					keyboard.append(keyoardRow)
					keyoardRow = []
				keyboard.sort()
				keyboard.append(["Back", "Start"])
				try:
					replyMsg = "command received"
					self.keybordForUsers[chat_id].append(keyboard)
					reply_markup = ReplyKeyboardMarkup.create(keyboard, one_time_keyboard= False, selective=True)
				except:
					replyMsg = "Restoring keyboard due to an internal error"
					self.keybordForUsers[chat_id] = [self.keyboards["start"]]
					reply_markup = ReplyKeyboardMarkup.create(self.keyboards["start"], one_time_keyboard= False, selective=True)
				

			elif (text in self.allDevicesList):
				replyMsg = "Connecting to device %s" %  (text)
				self.bot.send_message(chat_id, replyMsg , reply_markup=reply_markup)
				replyMsg, keyboard = self.retrieveDeviceInfo(text)
				
				try:
					#replyMsg = "command received"
					self.keybordForUsers[chat_id].append(keyboard)
					reply_markup = ReplyKeyboardMarkup.create(keyboard, one_time_keyboard= False, selective=True)
				except:
					replyMsg = "Restoring keyboard due to an internal error"
					self.keybordForUsers[chat_id] = [self.keyboards["start"]]
					reply_markup = ReplyKeyboardMarkup.create(self.keyboards["start"], one_time_keyboard= False, selective=True)
				
			elif (text in self.alldevicesFunctionsList):
				replyMsg = "Connecting to device %s" % (self.alldevicesFunctionsList[text]) #(text)
				self.bot.send_message(chat_id, replyMsg , reply_markup=reply_markup)
				replyMsg, keyboard = self.retrieveDeviceInfo(self.alldevicesFunctionsList[text])
				reply_markup = ReplyKeyboardMarkup.create(keyboard, one_time_keyboard= False, selective=True)
				# do not save the current keyboard in self.keybordForUsers[chat_id].append(keyboard)
				# otherwaise the "Back" button engine will not work properly 


			else:
				replyMsg = 'Unkonwn command "%s"' % (text)

			self.bot.send_message(chat_id, replyMsg , reply_markup=reply_markup)
							 
		else:
			last_name = update.message.chat.last_name
			username = update.message.chat.username
			replyMsg += "user"
			if (name is not None):
				replyMsg += " %s" % (name) 

			if (last_name is not None):
				replyMsg += " %s" % (last_name)

			if (username is not None):
				replyMsg += " with username %s" % (username)

			replyMsg += " (userID: %s) is not allowed" % (chat_id)

			self.logger.error(replyMsg)
			self.bot.send_message(chat_id, replyMsg)
					
         		

	# chat_bot allows to answer all clients requests 
	def chat_bot(self,chat_id,payload):
		pass


	# used to send allarms to specific clients
	def send_allarm_bot(self,chat_id,mess):
		self.bot.send_message(chat_id=chat_id, text=mess)

	def retrieveHomeSettings(self):
		super(SmartHomeBot, self).retrieveHomeSettings()
		try:
			self.botToken = self.myhome["TelegramBot"]["telegramToken"]
			self.validUsers = self.myhome["TelegramBot"]["allowedUserID"]	
			self.makeKeyboards()
		except (Exception, e):
			self.logger.error ("Error on retrieveHomeSettings: %s" % (e))


if __name__ == "__main__":
	mybot = SmartHomeBot("SmartHomeTelegramBot", logLevel)
	mybot.start()
