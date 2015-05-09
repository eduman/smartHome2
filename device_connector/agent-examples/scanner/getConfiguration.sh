#!/bin/bash

IFCONFIG="`whereis ifconfig | awk /ifconfig/'{split($0,a," ");print a[2];exit}'`" 
AWK="`whereis awk | awk /awk/'{split($0,a," ");print a[2];exit}'`"

INTERFACE="eth0"  # scheda ethernet
IP="`$IFCONFIG $INTERFACE | $AWK /$INTERFACE/'{next}//{split($0,a,":");split(a[2],a," ");print a[1];exit}'`"
NETMASK="`$IFCONFIG $INTERFACE | $AWK /$INTERFACE/'{next}//{split($0,a,":");split(a[4],a," ");print a[1];exit}'`"
GATEWAY=$(ip route list | sed -n -e "s/^default.*[[:space:]]\([[:digit:]]\+\.[[:digit:]]\+\.[[:digit:]]\+\.[[:digit:]]\+\).*/\1/p")

RESULT=("{\"configured\": true,\"ip\": \"$IP\",\"subnet\": \"$NETMASK\",\"gateway\": \"$GATEWAY\",\"description\": \"scanner\",\"isError\": false,\"functions\": [{\"type\": \"Scan\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/scanner/scan\"}]}")
echo "$RESULT"
