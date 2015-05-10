#!/usr/bin/env python
import threading

class Context ():

	def __init__(self, id):
		self.id = id
		self.lock = threading.Lock()
		self.properties = {}

	def getID (self):
		return self.id
	
	def getProperty(self, key):
		result = self.properties.get(key.lower())
		return result

	def getProperties(self):
		return self.properties

	def updateProperty (self, key, value):
		try:
			self.lock.acquire()
			self.properties[str(key.lower())]=str(value)
			self.lock.release()
		except Exception, e:
			self.logger.error("Erroron Context.updateProperty(): %s" % (e))
	def getKeys (self):
		return self.properties.keys()


if __name__ == "__main__":
	c = Context(2)
	print (c.getProperty("key"))
	c.updateProperty("1","1")
	print (c.getProperty("1"))
	print(c.getKeys())
	print (c.getProperties())