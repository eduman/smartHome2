# Architectural dependencies

All subscribers logically depend from the home proxy (https://github.com/eduman/smartHome2/tree/master/device_connector/agents/home). Indeed, they need to download the right configuration setting specified and stored in home_structure.json

In *Subscriber.py, the home proxy web service must be updated (variable homeWSUri)
