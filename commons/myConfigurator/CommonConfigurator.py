import ConfigParser
import os


def getCommonSettingsSection():
	return "common_settings"

def getHomeEndPoint():
	return "home_endpoint"

def getPlugwiseEndPoint():
	return "plugwise_endpoint"


def getHomeEndPointValue(configPath):
	try:
		return getConfigurator(configPath).get(getCommonSettingsSection(), getHomeEndPoint())
	except Exception, e:
		raise Exception(e)

def getPlugwiseEndPointValue(configPath):
	try:
		return getConfigurator(configPath).get(getCommonSettingsSection(), getPlugwiseEndPoint())
	except Exception, e:
		raise Exception(e)



def makeDefaultConfigFile(configPath):
	try:
		os.makedirs(os.path.dirname(configPath))
	except Exception, e:
		pass

	f = open(configPath, "w+")

	#ConfigParser.SafeConfigParser.add_comment = lambda self, section, option, value: self.set(section, '\n; '+option, value)

	config = ConfigParser.SafeConfigParser()
	
	section = getCommonSettingsSection()
	config.add_section(section)
	config.set(section, getHomeEndPoint(), "http://localhost:8080/rest/home/configuration")
	config.set(section, getPlugwiseEndPoint(), "http://localhost:8083")

	config.write(f)


def getConfigurator (configPath):
	config = ConfigParser.SafeConfigParser()
	try: 
		if not os.path.exists(configPath):
			makeDefaultConfigFile(configPath)

		config.read(configPath)
		return config
	except Exception, e:
		raise Exception("Error on CommonConfigurator.getConfigurator(): %s" % (e))


