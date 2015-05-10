

class Device:
	def __init__(self, id, value):
		self.id = id
		self.value = value

	def getID(self):
		return self.id
	
	def setID (self, id):
		self.id = id 

	def getValue (self):
		return self.value

	def setValue (self, value):
		self.value = value
