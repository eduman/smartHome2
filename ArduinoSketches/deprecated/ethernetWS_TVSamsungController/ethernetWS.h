#ifndef ETHERNETWS_H
#define ETHERNETWS_H

// constants
//const boolean firstBoot = false;

const String fieldSeparator = ";";
const String rowSeparator = "\n";
const String coupleSeparator = "=";
const String andSeparator = "&";
const String errorStr = "error";
const String successStr = "success";
const String getCommandStr = "get"+andSeparator;
const String setCommandStr = "set"+andSeparator;
//const String getPinConfigurationStr = "/getPinConfiguration";
const String getPinConfigurationStr = "/getPin";
//const String getNetConfigurationStr = "/getNetConfiguration";
const String getNetConfigurationStr = "/getNet";
const String setNetConfigurationStr = "setNetConfiguration";
const String moteStr = "mote";
const String relayStr = "relay";
const String statusStr = "status";
const String valueStr = "value";
const String commandErr = "The requested command does not exist!";

#endif



