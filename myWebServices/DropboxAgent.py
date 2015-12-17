#!/usr/bin/python
# -*- coding: iso-8859-15 -*-
import logging
import os
import datetime
import time
import os, sys
import subprocess
import stat
from threading import Thread
import WebServicesConfigurationConstants as WSConstants

# dependencies: dropbox SDK
# sudo pip install dropbox
import dropbox




class DropboxAgent(object):

	def __init__(self, serviceName, logLevel, userId, accessToken):
		self.serviceName = serviceName
		self.accessToken = accessToken
		self.userId = userId


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

		self.isRunning = False
		



	def start (self, localFolder, remoteFolder):

		self.localFolder = localFolder
		self.remoteFolder = remoteFolder
		if not os.path.exists(self.localFolder):
			try:
				os.makedirs(self.localFolder)
			except Exception, e:
				self.logger.error("Error in creating image localFolder: %s" % (e))

		self.logger.info("Started")

		if self.accessToken == WSConstants.getDefaultDropboxAccessToken() or self.accessToken is None:
			self.logger.error ("Unable to start DropboxAgent. Run DropboxAgent (python DropboxAgent.py) to retrieve your userID and accessToken")
		else:
			self.client = dropbox.client.DropboxClient(self.accessToken)
			self.isRunning = True

			try:
				if hasattr (self, "client"):
					if (self.isRunning):
						t1 = Thread(target=self.upload)
						t1.start()
				else:
					self.logger.error ("Unable to start DropboxAgent.")
			except:
				pass



	def stop(self):
		self.isRunning = False
		self.logger.info("Ended")



	def upload(self):
		try:

			while (self.isRunning):
				onlyfiles = [ f for f in os.listdir(self.localFolder) if os.path.isfile(os.path.join(self.localFolder,f)) ]
				for file in onlyfiles:
					filePath = self.localFolder + '/' + file
					f = open(filePath, 'rb')
					response = self.client.put_file(self.remoteFolder + '/' + file, f)		
					os.remove(filePath)
					self.logger.info('file \"' + file + '\" uploaded and deleted!')

				time.sleep(5)

		except Exception, e:
			self.logger.error('Failed to upload to dropbox: '+ str(e))
		except:
			pass

		




if __name__ == "__main__":
	# Get your app key and secret from the Dropbox developer website
	app_key = 'your_app_key'
	app_secret = 'your_app_secret'

	flow = dropbox.client.DropboxOAuth2FlowNoRedirect(app_key, app_secret)
	authorize_url = flow.start()
	print '1. Go to: ' + authorize_url
	print '2. Click "Allow" (you might have to log in first)'
	print '3. Copy the authorization code.'
	code = raw_input("Enter the authorization code here: ").strip()
	access_token, user_id = flow.finish(code)
	print 'save your userId and accessToken to your "myWebService.conf"'
	print 'userId: ' + str(user_id)
	print 'accessToken: ' + str(access_token)




