#!/usr/bin/env python

# Copyright (C) 2011 Sven Petai <hadara@bsd.ee> 
# Use of this source code is governed by the MIT license found in the LICENSE file.


import optparse
import logging
from serial.serialutil import SerialException
import ConfigParser
import socket
import fcntl
import struct

from plugwise import *
import plugwise.util

DEFAULT_SERIAL_PORT = "/dev/ttyUSB0"
PORT_WS="8080"
ETHERNET="eth0"

wsBase = "http://%s:%s/rest/plugwise/%s/%s"
jsonMsg = '{"configured":true,"ip":"%s","subnet":"","gateway":"","port":"","isError":false,"functions":[%s]}'
function = '{"pin":%s,"type":"%s","configuredAs":"%s","status":"%s","unit":"%s","rest": "GET","ws":"%s"}'
appliance = "N/A appliance"

logger = logging.getLogger('plugwise')
hdlr = logging.FileHandler('log/plugwise.log')
formatter = logging.Formatter("%(asctime)s [%(threadName)-12.12s] [%(levelname)-5.5s]  %(message)s")
hdlr.setFormatter(formatter)
logger.addHandler(hdlr)
logger.setLevel(logging.INFO)

parser = optparse.OptionParser()

parser.add_option("-m", "--mac", dest="mac", help="MAC address")
parser.add_option("-d", "--device", dest="device", 
    help="Serial port device")
parser.add_option("-p", "--power", action="store_true", 
    help="Get current power usage")
parser.add_option("-s", "--switch", dest="switch", 
    help="Switch power on/off. Possible values: 1,on,0,off")
parser.add_option("-i", "--info", action="store_true", dest="info", 
    help="Perform the info request")


options, args = parser.parse_args()

device = DEFAULT_SERIAL_PORT

if options.device:
    device = options.device

if not options.mac:
    logger.error("you have to specify mac with -m")


def getConfig (c):
    try:
        power = "%.2f" % (c.get_power_usage())
    except ValueError:
        power = "0.00"

#GET LAST 4 VALUES OF CURRENT FROM THE CIRCLE BUFFER
#    for dt, watt_hours in c.get_power_usage_history(None):
#        if dt is None:
#            ts_str,watt_hours = "N/A", "N/A"
#        else:
#            ts_str = dt.strftime("%Y-%m-%d %H")
#            watt_hours = "%f" % (watt_hours,)
#        print("\t%s %s Wh" % (ts_str, watt_hours))


    values = ""
    status = str(c.get_info())[146:147]
    ip=get_ip_address(ETHERNET)
    plugwiseID = options.mac.lower()

    if status == "1":
        switchWS = wsBase % (ip, PORT_WS, plugwiseID, "off")
    else:
        switchWS = wsBase % (ip, PORT_WS, plugwiseID, "on")
    
    infoWS= wsBase % (ip, PORT_WS, plugwiseID, "configuration")
    values += function % (1, appliance, "switch", status, "", switchWS) + ","
    values += function % (2, "Power", "sensor", power, "W", infoWS) 

    msg = jsonMsg % (plugwiseID, values)
    print (msg)

def setConfig():
    try:
        Config = ConfigParser.ConfigParser()
        Config.read("conf/agents/plugwise_circles.conf")
        global appliance
        appliance = Config.get("circles", options.mac)
    except Exception as e:
        logger.error("Error on reading config file: %s" % (e))

def get_ip_address(ifname):
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    return socket.inet_ntoa(fcntl.ioctl(
        s.fileno(),
        0x8915,  # SIOCGIFADDR
        struct.pack('256s', ifname[:15])
    )[20:24])

def main():
    setConfig()
    try:
        global device
        device = Stick(device)
        c = Circle(options.mac, device)

        if options.info:
            getConfig(c)

        elif options.switch:
            sw_direction = options.switch.lower()

            if sw_direction in ('on', '1'):
                c.switch_on()
            elif sw_direction in ('off', '0'):
                c.switch_off()
            else:
                logger.error("Error: Unknown switch direction: "+sw_direction)


            getConfig(c)


    except (TimeoutException, SerialException) as reason:
        logger.error("Error: %s" % (reason,))

if __name__ == "__main__":
    main()