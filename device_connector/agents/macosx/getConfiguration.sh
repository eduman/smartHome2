#!/bin/bash

#IFCONFIG="`whereis ifconfig | awk /ifconfig/'{split($0,a," ");print a[2];exit}'`" 
#AWK="`whereis awk | awk /awk/'{split($0,a," ");print a[2];exit}'`"

INTERFACE="en0"  
#IP="`$IFCONFIG $INTERFACE | $AWK /$INTERFACE/'{next}//{split($0,a,":");split(a[2],a," ");print a[1];exit}'`"
#NETMASK="`$IFCONFIG $INTERFACE | $AWK /$INTERFACE/'{next}//{split($0,a,":");split(a[4],a," ");print a[1];exit}'`"
#GATEWAY=$(ip route list | sed -n -e "s/^default.*[[:space:]]\([[:digit:]]\+\.[[:digit:]]\+\.[[:digit:]]\+\.[[:digit:]]\+\).*/\1/p")

IP="`ipconfig getifaddr $INTERFACE`"
NETMASK=""
GATEWAY=""

Test="`osascript -e 'output muted of (get volume settings)'`"
if [ $Test = "true" ]; then
	RESULT=("{\"configured\": true,\"ip\": \"$IP\",\"subnet\": \"$NETMASK\",\"gateway\": \"$GATEWAY\",\"description\": \"macosx\",\"isError\": false,\"functions\": [{\"type\": \"BrightnessDown\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/brightnessdown\"},{\"type\": \"BrightnessUp\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/brightnessUp\"},{\"type\": \"Next\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/playernext\"},{\"type\": \"Play/Pause\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/playerplaypause\"},{\"type\": \"Previous\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/playerprevious\"},{\"type\": \"Stop\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/playerstop\"},{\"type\": \"VolumeDown\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/volumedown\"},{\"type\": \"VolumeUp\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/volumeup\"},{\"type\": \"Muted\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/volumemutedfalse\"},{\"type\": \"Sleep\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/sleep\"}]}")

else 
	RESULT=("{\"configured\": true,\"ip\": \"$IP\",\"subnet\": \"$NETMASK\",\"gateway\": \"$GATEWAY\",\"description\": \"macosx\",\"isError\": false,\"functions\": [{\"type\": \"BrightnessDown\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/brightnessdown\"},{\"type\": \"BrightnessUp\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/brightnessUp\"},{\"type\": \"Next\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/playernext\"},{\"type\": \"Play/Pause\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/playerplaypause\"},{\"type\": \"Previous\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/playerprevious\"},{\"type\": \"Stop\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/playerstop\"},{\"type\": \"VolumeDown\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/volumedown\"},{\"type\": \"VolumeUp\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/volumeup\"},{\"type\": \"Mute\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/volumemutedtrue\"},{\"type\": \"Sleep\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/sleep\"}]}")

fi

#RESULT=("{\"configured\": true,\"ip\": \"$IP\",\"subnet\": \"$NETMASK\",\"gateway\": \"$GATEWAY\",\"description\": \"macosx\",\"isError\": false,\"functions\": [{\"type\": \"BrightnessDown\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/brightnessdown\"},{\"type\": \"BrightnessUp\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/brightnessUp\"},{\"type\": \"Next\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/playernext\"},{\"type\": \"Play/Pause\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/playerplaypause\"},{\"type\": \"Previous\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/playerprevious\"},{\"type\": \"Stop\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/playerstop\"},{\"type\": \"VolumeDown\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/volumedown\"},{\"type\": \"VolumeUp\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/volumeup\"},{\"type\": \"Mute\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/volumemutedfalse\"},{\"type\": \"Mute\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/volumemutedtrue\"},{\"type\": \"Sleep\",\"configuredAs\": \"Button\",\"rest\":\"GET\",\"ws\":\"/rest/macosx/sleep\"}]}")
echo "$RESULT"




