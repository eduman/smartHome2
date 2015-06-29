# LocalConnect for SmartHome vs

Version of SmartHome compatible with the LinkSmartLocalConnect (https://linksmart.eu/redmine/projects/linksmart-local-connect), which is a new component of the LinkSmart middleware version 2.2
It includes the following proxies (ported form the java version compatible with LinkSmart middleware 1.3)
- dropbox proxy
- raspberry proxy
- scanner proxy

## External dependencies
the following libraries or external programs are needed:

- sane. Used by scanner proxy. "sudo apt-get install sane" (for installing in a raspberry pi)
- dropbox SDK python version. Used by dropbox proxy. "sudo pip install dropbox" 
- paho mqtt library python version. "sudo pip install paho-mqtt" 
- python-plugwise lybrary (https://bitbucket.org/hadara/python-plugwise/wiki/Home)
