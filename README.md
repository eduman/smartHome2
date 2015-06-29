# SmartHome v2

SmartHome version 2 is compatible with the LinkSmart LocalConnect (https://linksmart.eu/redmine/projects/linksmart-local-connect).

It includes:
- agents and example of configuration files for the linksmart device connector, which is part of the LinkSmart LocalConnect 
- control strategies
- mqtt publishers and subscribers wrappers for not mqtt-compliant IoT devices
- smartHome v2 commons libraries python version ("commons" folder)
- scketches for Arduino (xboard relay)
- bash script to run smartHome v2
- command examples

## External dependencies
the following libraries or external programs are needed:

- paho mqtt library python version. "sudo pip install paho-mqtt" 
- python-plugwise library (https://bitbucket.org/hadara/python-plugwise/wiki/Home)
- cherrypy library (sudo pip install cherrypy)
