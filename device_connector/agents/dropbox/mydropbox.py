#!/usr/bin/python



# dependencies: dropbox SDK
# sudo pip install dropbox


import sys, getopt
import dropbox
import os
import logging


logger = logging.getLogger('mydropbox')
hdlr = logging.FileHandler('log/mydropbox.log')
formatter = logging.Formatter("%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
hdlr.setFormatter(formatter)
logger.addHandler(hdlr)
consoleHandler = logging.StreamHandler()
consoleHandler.setFormatter(formatter)
logger.addHandler(consoleHandler)
logger.setLevel(logging.INFO)


# Get your app key and secret from the Dropbox developer website
app_key = 'your_app_key'
app_secret = 'your_app_secret'


# ENABLE THE FOLLOWING LIN TO HAVE A NEW TOKEN
#flow = dropbox.client.DropboxOAuth2FlowNoRedirect(app_key, app_secret)
#authorize_url = flow.start()
#print '1. Go to: ' + authorize_url
#print '2. Click "Allow" (you might have to log in first)'
#print '3. Copy the authorization code.'
#code = raw_input("Enter the authorization code here: ").strip()
#access_token, user_id = flow.finish(code)

access_token = "your_token"
user_id = "your_id"


client = dropbox.client.DropboxClient(access_token)
#print 'linked account: ', client.account_info()

def main(argv):
	localfoder = ''
	remotefolder = ''
	try:
		opts, args = getopt.getopt(argv,"hl:r:",["lfolder=","rfolder="])
	except getopt.GetoptError:
		logger.error('mydropbox.py -l <local_folder> -r <remote_folder>')
		sys.exit(2)

	for opt, arg in opts:
		if opt == '-h':
         		logger.error('mydropbox.py -l <local_folder> -r <remote_folder>')
         		sys.exit()
      		elif opt in ("-l", "--lfolder"):
       			localfolder = arg
      		elif opt in ("-r", "--rfolder"):
         		remotefolder = arg

	try:	
		onlyfiles = [ f for f in os.listdir(localfolder) if os.path.isfile(os.path.join(localfolder,f)) ]
		for file in onlyfiles:
			filePath = localfolder + '/' + file
			f = open(filePath, 'rb')
			response = client.put_file(remotefolder + '/' + file, f)		
			os.remove(filePath)
			logger.info('file \"' + file + '\" uploaded and deleted!')
	except Exception, e:
		logger.error('Failed to upload to dropbox: '+ str(e))

if __name__ == "__main__":
   main(sys.argv[1:])

# EXAMPLE TO DOWNLOAD A FILE
#folder_metadata = client.metadata('/')
#print 'metadata: ', folder_metadata
#f, metadata = client.get_file_and_metadata('/magnum-opus.txt')
#out = open('magnum-opus.txt', 'wb')
#out.write(f.read())
#out.close()
#print metadata

