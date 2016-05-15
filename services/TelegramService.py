# dependencies: twx.botapi
# sudo pip install -i https://testpypi.python.org/pypi twx.botapi

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

requests.packages.urllib3.disable_warnings()

logLevel = logging.INFO
homeWSUri = "http://localhost:8080/rest/home/configuration"
#homeWSUri = "http://192.168.1.5:8080/rest/home/configuration"

class SmartHomeBot:

	def __init__ (self):
		self.lastUpdateID = 0
		self.validUsers = []
		self.keyboards = {	#"start": [["Rooms" , "All Devices"], ["Switch off all devices", "Rules"]], 
							"start": [["Rooms" , "All Devices"], ["Switch off all devices"]], 
							"Rooms": [], 
							"All Devices": [] , 
							"Rules": []}
		self.allDevicesList = {}
		self.alldevicesFunctionsList = {}
		self.allRoomsList = {}
		self.logger = self.makeLogger ("SmartHomeTelegramBot")
		self.homeUpdateTimer = 300 # update every 5 minutes
		self.isRunning = True 

		for sig in (signal.SIGABRT, signal.SIGILL, signal.SIGINT, signal.SIGSEGV, signal.SIGTERM):
			signal.signal(sig, self.signal_handler)

	def signal_handler(self, signal, frame):
		self.stop_bot()

	def stop_bot(self):
		self.isRunning = False
		if (hasattr(self, "homeUpdateThread")):
			if self.homeUpdateThread.isAlive():
				try:
					self.homeUpdateThread._Thread__stop()
				except:
					self.logger.error(str(self.homeUpdateThread.getName()) + ' (update home thread) could not be terminated')
		
		self.logger.info("bye bye")
		sys.exit(0)

	# Starts the communication with the bot
	def start_bot(self):
		self.logger.info("SmartHomeBot started")
		self.retrieveHomeSettings()

		if (self.botToken is None):
			self.logger.error ("The Telegram Token is not valid")
			self.stop_bot()
		else:
			self.bot = TelegramBot(self.botToken)
			self.homeUpdateThread = Thread (target = self.homeUpdate)
			self.homeUpdateThread.start()

			# serving clients
			try:
				while self.isRunning:
					#The get_updates method returns the earliest 100 unconfirmed updates
					updates = self.bot.get_updates(offset = self.lastUpdateID + 1).wait()
					cont1=len(updates)
					if cont1 != 0:
						replyThread = Thread (target = self.reply, args=[updates[-1]])
						replyThread.start()
						self.lastUpdateID = updates[-1].update_id

					time.sleep(1)
			except KeyboardInterrupt:
				self.stop_bot()


	def retrieveHomeSettings(self):
		resp, isOk = self.invokeWebService(homeWSUri)
		while (not isOk):
			self.logger.error ("Unable to find the home proxy. I will try again in a while...")
			resp, isOk = self.invokeWebService(homeWSUri)
			time.sleep(10) #sleep 10 seconds
		try:
			self.myhome = json.loads(resp)
			self.botToken = self.myhome["TelegramBot"]["telegramToken"]
			self.validUsers = self.myhome["TelegramBot"]["allowedUserID"]	
			self.makeKeyboards()
		except (Exception, e):
			self.logger.error ("Error on retrieveHomeSettings: %s" % (e))

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
				label = "%s - %s" % (device["deviceID"], device["description"])
				self.allDevicesList[label] = device
				keyoardRow.append (label)
				keyboard.append(keyoardRow)
				keyoardRow = []


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
		if (deviceID.lower().startswith("http://") or deviceID.lower().startswith("https://")):
			deviceUri = deviceID
		else:
			deviceUri = self.allDevicesList[deviceID]["protocol"][0]["uri"]

		resp, isOk = self.invokeWebService (deviceUri)
		replyMsg = ""
		reply_markup = None
		if (isOk):
			deviceInfo = json.loads (resp)
			keyboard = []
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
					label = "%s - %s %s" % (deviceInfo["ip"], nextStatus, function["type"])
					self.alldevicesFunctionsList[label] = function["ws"]
					keyboard.append([label])

				elif (function["configuredAs"].lower() == "button"):
					replyMsg += "%s\n" % (function["type"])
					label = "%s - %s" % (deviceInfo["ip"], function["type"])
					self.alldevicesFunctionsList[label] = function["ws"]
					keyboard.append([label])

				elif (function["configuredAs"].lower() == "sensor"):
					replyMsg += "%s is %s%s\n" % (function["type"], function["status"], function["unit"])

			if (len(keyboard)>0):
				keyboard.append(["Back", "Start"])
				#reply_markup = ReplyKeyboardMarkup.create(keyboard, one_time_keyboard= False, selective=True)


		else:
			replyMsg = 'Devices %s is unreachable: %s' % (deviceID, resp)
		
		return replyMsg, keyboard


	def homeUpdate(self):
		while (self.isRunning):
			time.sleep(self.homeUpdateTimer)
			self.retrieveHomeSettings()


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


	def reply (self, update):
		chat_id=update.message.chat.id
		name = update.message.chat.first_name
		replyMsg = ""
		if chat_id in self.validUsers:  
			text = update.message.text
			reply_markup = None

			if (text == "/start"):
				replyMsg += "Hi %s! Welcome on SmartHome2!" % (name) 
				reply_markup =ReplyKeyboardMarkup.create(self.keyboards["start"], one_time_keyboard= False, selective=True)
				
			elif (text == "Start"):
				replyMsg = "command received" 
				reply_markup =ReplyKeyboardMarkup.create(self.keyboards["start"], one_time_keyboard= False, selective=True)

			elif (text == "Back"):
				replyMsg = "command to be implemented" 
				

			elif (text == self.keyboards["start"][0][0]):
				# Rooms
				replyMsg = "command received"
				reply_markup = ReplyKeyboardMarkup.create(self.keyboards[self.keyboards["start"][0][0]], one_time_keyboard= False, selective=True)

			elif (text == self.keyboards["start"][0][1]):
				# Devices
				replyMsg = "command received"
				reply_markup = ReplyKeyboardMarkup.create(self.keyboards[self.keyboards["start"][0][1]], one_time_keyboard= False, selective=True)

			elif (text == self.keyboards["start"][1][0]):
				# Switch off all devices
				replyMsg = "command received. The execution may take some time. I will keep you updated."
				self.bot.send_message(chat_id, replyMsg , reply_markup=reply_markup)
				resp, isOk = self.invokeWebService (self.myhome["switchOffAllDevicesAgent"])
				if (isOk):
					replyMsg = "command sent to devices"
				else:
					replyMsg = 'Unable to send "switch off" command to devices: %s' % (resp)
				#reply_markup = ReplyKeyboardMarkup.create(self.keyboards["allDevices"], one_time_keyboard= False, selective=True)
			
#			elif (text == self.keyboards["start"][1][1]):
#				# Rules
#				replyMsg = "command received"
#				reply_markup = ReplyKeyboardMarkup.create(self.keyboards[self.keyboards["start"][1][0]], one_time_keyboard= False, selective=True)
			
			elif (text == "/update"):
				resp, isOk = self.invokeWebService(homeWSUri)
				if (isOk):
					self.myhome = json.loads(resp)
					self.botToken = self.myhome["TelegramBot"]["telegramToken"]
					self.validUsers = self.myhome["TelegramBot"]["allowedUserID"]	
					self.makeKeyboards()
					replyMsg = "Bot Updated"
				else:
					replyMsg = "Unable to update the bot: %s" % (resp)

			elif (text in self.allRoomsList):
				# Devices per room keyboard
				keyboard = []
				keyoardRow = []
				for count, device in  enumerate(self.allRoomsList[text]["devices"]):
					label = "%s - %s" % (device["deviceID"], device["description"])
					self.allDevicesList[label] = device
					keyoardRow.append (label)
					keyboard.append(keyoardRow)
					keyoardRow = []
				keyboard.append(["Back", "Start"])
				replyMsg = "command received"
				reply_markup = ReplyKeyboardMarkup.create(keyboard, one_time_keyboard= False, selective=True)


			elif (text in self.allDevicesList):
				replyMsg = "Connecting to device %s" %  (text)
				self.bot.send_message(chat_id, replyMsg , reply_markup=reply_markup)
				replyMsg, keyboard = self.retrieveDeviceInfo(text)
				reply_markup = ReplyKeyboardMarkup.create(keyboard, one_time_keyboard= False, selective=True)
				
			elif (text in self.alldevicesFunctionsList):
				replyMsg = "Connecting to device %s" % (self.alldevicesFunctionsList[text]) #(text)
				self.bot.send_message(chat_id, replyMsg , reply_markup=reply_markup)
				replyMsg, keyboard = self.retrieveDeviceInfo(self.alldevicesFunctionsList[text])
				reply_markup = ReplyKeyboardMarkup.create(keyboard, one_time_keyboard= False, selective=True)

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


	def makeLogger(self, serviceName):
		logPath = "log/%s.log" % (serviceName)
		if not os.path.exists(logPath):
			try:
				os.makedirs(os.path.dirname(logPath))
			except Exception, e:
				pass	

		logger = logging.getLogger(serviceName)
		logger.setLevel(logLevel)
		hdlr = logging.FileHandler(logPath)
		formatter = logging.Formatter(serviceName + ": " + "%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
		hdlr.setFormatter(formatter)
		logger.addHandler(hdlr)
		
		consoleHandler = logging.StreamHandler()
		consoleHandler.setFormatter(formatter)
		logger.addHandler(consoleHandler)
		return logger

if __name__ == "__main__":
	mybot = SmartHomeBot()
	mybot.start_bot()

	#"telegramToken": "185527325:AAHBbylTmQ09vjXQXctKG4eiitd8Di6TISM",
	#"allowedUserID": [187393864, 199822101]