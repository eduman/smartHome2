#!/usr/bin/python

import json
import random
import sys
import shutil
import os,sys

filename = '/home_structure.json'
fullpath = (os.path.dirname(os.path.realpath(__file__)) + os.sep) + filename

filename2 = '/home_structure2.json'
fullpath2 = (os.path.dirname(os.path.realpath(__file__)) + os.sep) + filename2


def main(argv):
	
	try:
		json_data=open(fullpath).read()
		localHome = json.loads(json_data)

		remoteHome = json.loads(argv[0])

		for localRule in localHome['rules']:
			for remoteRule in remoteHome['rules']:
				if localRule['ruleSID'] == remoteRule['ruleSID']:
					localRule['isRuleEnabled'] = remoteRule['isRuleEnabled'] 
				
		

		f = open(fullpath2,'w')
		#f = open(fullpath,'w')
		string = json.dumps(localHome)
		f.write(string)
		f.close()
	except Exception, e:
		print e

if __name__ == '__main__':
	main(sys.argv[1:])