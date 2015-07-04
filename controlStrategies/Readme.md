# Architectural dependencies

All the control strategies logically depend from the home proxy (https://github.com/eduman/smartHome2/tree/master/device_connector/agents/home). Indeed, they need to download the right configuration setting specified and storend in home_structure.json

In *ControlStrategy.py, the RuleID and home proxy web service must be specified (variables ruleSID and homeWSUri respectively )
