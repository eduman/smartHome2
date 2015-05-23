#!/bin/bash

IFCONFIG="`whereis ifconfig | awk /ifconfig/'{split($0,a," ");print a[2];exit}'`" 
AWK="`whereis awk | awk /awk/'{split($0,a," ");print a[2];exit}'`"

INTERFACE="eth0"  # scheda ethernet
IP="`$IFCONFIG $INTERFACE | $AWK /$INTERFACE/'{next}//{split($0,a,":");split(a[2],a," ");print a[1];exit}'`"
NETMASK="`$IFCONFIG $INTERFACE | $AWK /$INTERFACE/'{next}//{split($0,a,":");split(a[4],a," ");print a[1];exit}'`"
GATEWAY=$(ip route list | sed -n -e "s/^default.*[[:space:]]\([[:digit:]]\+\.[[:digit:]]\+\.[[:digit:]]\+\.[[:digit:]]\+\).*/\1/p")
PORT="8080"

TEMP=$(/opt/vc/bin/vcgencmd measure_temp| egrep "[0-9.]{4,}" -o)

RESULT=("{\"configured\": true,\"ip\": \"$IP\",\"subnet\": \"$NETMASK\",\"gateway\": \"$GATEWAY\",\"port\":\"$PORT\",\"description\": \"raspberry pi\",\"isError\": false,\"functions\": [{\"pin\":1,\"type\": \"SoC Temperature\",\"configuredAs\": \"Sensor\",\"status\":\"$TEMP\",\"unit\":\"°C\",\"rest\":\"GET\",\"ws\":\"http://$IP:$PORT/rest/raspberry/temperature\"}]}")
echo "$RESULT"




