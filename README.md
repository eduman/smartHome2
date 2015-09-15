# SmartHome v2

SmartHome version 2 is compatible with the LinkSmart LocalConnect (https://linksmart.eu/redmine/projects/linksmart-local-connect).

It includes:
- agents and example of configuration files for the linksmart device connector, which is part of the LinkSmart LocalConnect 
- myWebServices,a web services conteinar for the smartHome agents. It is an alternative to LinkSmart LocalConnect 
- control strategies
- mqtt publishers and subscribers wrappers for not mqtt-compliant IoT devices
- smartHome v2 commons libraries python version ("commons" folder)
- scketches for Arduino (xboard relay)
- bash script to run smartHome v2
- command examples
- mobileHome2 (Android GUI)

## Third Party software 

SmartHome v2 integrates freeboard.io dashboard (http://freeboard.io/), which is an open source software.

## External dependencies
the following libraries or external programs are needed:

- paho mqtt library python version. (sudo pip install paho-mqtt)
- python-plugwise library (https://bitbucket.org/hadara/python-plugwise/wiki/Home)
- pyserial library (sudo pip install pyserial)
- cherrypy library (sudo pip install cherrypy)
