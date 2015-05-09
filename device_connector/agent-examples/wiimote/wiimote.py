#!/usr/bin/python


import cwiid
import time
import paho.mqtt.client as paho
from device import Device
import signal
import sys


isDebug = True

button_delay = 1
host = "192.168.1.254"
port = "1883"
qos = 2
action_topic = "ACTION/ACTUATOR/%s"
json = '{"topic":"%s", "value":"%s", "function":"%s", "device":"%s", "timestamp":null}'
server_message = '{"status":"%s", "message":"%s"}'
client=paho.Client()
msg = ""
topic = ""

devices ={cwiid.BTN_LEFT : Device("ArduinoProxy:eduman:Magneto:3:7", True)} 
devices.update ({cwiid.BTN_RIGHT : Device("ArduinoProxy:eduman:Magneto:3:8", True)})

def signal_handler(signal, frame):
	debug ('You pressed Ctrl+C!')
	sys.exit(0)

def debug(msg):
	if isDebug:
		print server_message % ("ok", msg)

def make_msg(topic, value, function, device):
	global msg
	msg = json % (topic, value, function, device)

def actuate (button):
	if devices.has_key(button):
		make_topic(devices.get(button).getID())
	        if devices.get(button).getValue():
		        value = "switchOn"
        	        devices.get(button).setValue(False)
        	else :
        		value = "switchOff"
	                devices.get(button).setValue(True)
		make_msg (topic, value, value, devices.get(button).getID())
       		myrun()

def make_topic (device):
	global topic
	topic = action_topic % (device)

def on_connect(pahoClient, obj, rc):
        # Once connected, publish message
        debug ("Connected Code = %d"%(rc))
        client.publish(topic, msg, qos)


def on_log(pahoClient, obj, level, string):
        debug(string)

def on_publish(pahoClient, packet, mid):
        # Once published, disconnect
        debug("Published")
        pahoClient.disconnect()

def on_disconnect(pahoClient, obj, rc):
        debug("Disconnected")

def myrun():
        #client=paho.Client()
        # Register callbacks
        client.on_connect = on_connect
        client.on_log = on_log
        client.on_publish = on_publish
        client.on_disconnnect =on_disconnect
        #Set userid and password
        #client.username_pw_set(userID, password)
        #connect
        x = client.connect(host, port, 60)
        client.loop_forever()

signal.signal(signal.SIGINT, signal_handler)

#print 'Press 1 + 2 on your Wii Remote now ...'
print server_message % ("ok", "Press 1 + 2 on your Wii Remote now ...")
time.sleep(1)

# Connect to the Wii Remote. If it times out
# then quit.
try:
	wii=cwiid.Wiimote()
except RuntimeError:
#	print "Error opening wiimote connection"
	print server_message % ("err", "Error opening wiimote connection")
	quit()

#print 'Wii Remote connected...\n'
#print 'Press PLUS and MINUS together to disconnect and quit.\n'

wii.rpt_mode = cwiid.RPT_BTN
 
while True:
	buttons = wii.state['buttons']

	# If Plus and Minus buttons pressed
	# together then rumble and quit.
	#if (buttons - cwiid.BTN_PLUS - cwiid.BTN_MINUS == 0):  
	#	print '\nClosing connection ...'
	#	wii.rumble = 1
    	#	time.sleep(1)
    	#	wii.rumble = 0
    	#	exit(wii)  
  
  	# Check if other buttons are pressed by
  	# doing a bitwise AND of the buttons number
  	# and the predefined constant for that button.
  	

	if (buttons & cwiid.BTN_LEFT):
    		debug ('Left pressed')
		actuate(cwiid.BTN_LEFT)
    		time.sleep(button_delay)         

  	if(buttons & cwiid.BTN_RIGHT):
    		debug('Right pressed')
		actuate(cwiid.BTN_RIGHT)
    		time.sleep(button_delay)          

  	if (buttons & cwiid.BTN_UP):
    		debug('Up pressed')
		actuate(cwiid.BTN_UP)        
    		time.sleep(button_delay)          
    
  	if (buttons & cwiid.BTN_DOWN):
    		debug ('Down pressed')      
    		actuate(cwiid.BTN_DOWN)
		time.sleep(button_delay)  
    
  	if (buttons & cwiid.BTN_1):
    		debug ('Button 1 pressed')
		actuate(cwiid.BTN_1)
    		time.sleep(button_delay)          

  	if (buttons & cwiid.BTN_2):
    		debug ('Button 2 pressed')
		actuate(cwiid.BTN_2)
    		time.sleep(button_delay)          

  	if (buttons & cwiid.BTN_A):
    		debug ('Button A pressed')
		actuate(cwiid.BTN_A)
    		time.sleep(button_delay)          

  	if (buttons & cwiid.BTN_B):
    		debug ('Button B pressed')
		actuate(cwiid.BTN_B)
    		time.sleep(button_delay)          

  	if (buttons & cwiid.BTN_HOME):
    		debug('Home Button pressed')
		actuate(cwiid.BTN_HOME)
    		time.sleep(button_delay)           
    
  	if (buttons & cwiid.BTN_MINUS):
    		debug('Minus Button pressed')
		actuate(cwiid.BTN_MINUS)
    		time.sleep(button_delay)   
    
  	if (buttons & cwiid.BTN_PLUS):
    		debug ('Plus Button pressed')
		actuate(cwiid.BTN_PLUS)
    		time.sleep(button_delay)

