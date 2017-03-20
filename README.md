# SmartHome v2

SmartHome version 2  includes:
- myWebServices, a set of web services for smartHome 
- control strategies
- mqtt publishers and subscribers wrappers for not mqtt-compliant IoT devices
- telegram service
- AmazonDashSevice, which integrates amazon wireless buttons and associates action commands to their clicks
- smartHome v2 commons libraries python version ("commons" folder)
- sketches for Arduino (xboard relay)
- bash script to run smartHome v2
- command examples
- mobileHome2 (Android GUI)

## Third Party software 

SmartHome v2 integrates freeboard.io dashboard (http://freeboard.io/), which is an open source software.

## External dependencies
the following libraries or external programs are needed:

- screen (sudo apt-get install screen)
- simplejson library (sudo pip install simplejson)
- paho mqtt library python version. (sudo pip install paho-mqtt)
- python-plugwise library (https://bitbucket.org/hadara/python-plugwise/wiki/Home)
- pyserial library (sudo pip install pyserial)
- cherrypy library (sudo pip install cherrypy)
- dropbox python SDK (sudo pip install dropbox)
- scapy library (sudo apt-get install scapy tcpdump tcpreplay wireshark python-scapy)
- ipaddress library (sudo pip install ipaddress)
- miniupnpc library (sudo pip install miniupnpc)
- enum34 library (sudo pip install enum34)
- twx.botapi - Telegram Unofficial Library (sudo pip install -i https://testpypi.python.org/pypi twx.botapi)
- Adafruit_Python_DHT (for raspberry pi)

### Howto install Adafruit_Python_DHT library on a raspberry pi

- sudo apt-get update
- sudo apt-get install build-essential python-dev 
- git clone https://github.com/adafruit/Adafruit_Python_DHT.git
- cd Adafruit_Python_DHT
- (if you want to execute dht driver as normal user change "/dev/mem" into "/dev/gpiomem" in Adafruit_Python_DHT/source/Raspberry_Pi/pi_mmio.c and Adafruit_Python_DHT/source/Raspberry_Pi_2/pi_2_mmio.c)
- sudo python setup.py install

