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

PORT="8080"
WSBASE="http://$IP:$PORT/rest/macosx"
BRIGHTNESS_DOWN_WS="$WSBASE/brightnessdown"
BRIGHTNESS_UP_WS="$WSBASE/brightnessup"
PLAYER_NEXT_WS="$WSBASE/playernext"
PLAYER_PLAY_PAUSE_WS="$WSBASE/playerplaypause"
PLAYER_PREVIOUS_WS="$WSBASE/playerprevious"
PLAYER_STOP_WS="$WSBASE/playerstop"
VOLUME_DOWN_WS="$WSBASE/volumedown"
VOLUME_UP_WS="$WSBASE/volumeup"

IS_MUTED="`osascript -e 'output muted of (get volume settings)'`"
if [ $IS_MUTED = "true" ]; then
	VOLUME_MUTE_WS="$WSBASE/volumemutedfalse"
	VOLUME_MUTE_STATUS="Muted"
else 
	VOLUME_MUTE_WS="$WSBASE/volumemutedtrue"
	VOLUME_MUTE_STATUS="ToBeMouted"
fi





RESULT=("{\"configured\": true,\"ip\": \"$IP\",\"subnet\": \"$NETMASK\",\"gateway\": \"$GATEWAY\",\"port\":\"$PORT\",\"description\": \"macosx\",\"isError\": false,\"functions\": [
{\"pin\":0,\"type\": \"BrightnessDown\",\"configuredAs\": \"Button\",\"status\":\"ok\",\"unit\":\"\",\"rest\":\"GET\",\"ws\":\"$BRIGHTNESS_DOWN_WS\"},
{\"pin\":1,\"type\": \"BrightnessUp\",\"configuredAs\": \"Button\",\"status\":\"ok\",\"unit\":\"\",\"rest\":\"GET\",\"ws\":\"$BRIGHTNESS_UP_WS\"},
{\"pin\":2,\"type\": \"PlayerNext\",\"configuredAs\": \"Button\",\"status\":\"ok\",\"unit\":\"\",\"rest\":\"GET\",\"ws\":\"$PLAYER_NEXT_WS\"},
{\"pin\":3,\"type\": \"PlayerPlayPause\",\"configuredAs\": \"Button\",\"status\":\"ok\",\"unit\":\"\",\"rest\":\"GET\",\"ws\":\"$PLAYER_PLAY_PAUSE_WS\"},
{\"pin\":4,\"type\": \"PlayerPrevious\",\"configuredAs\": \"Button\",\"status\":\"ok\",\"unit\":\"\",\"rest\":\"GET\",\"ws\":\"$PLAYER_PREVIOUS_WS\"},
{\"pin\":5,\"type\": \"PlayerStop\",\"configuredAs\": \"Button\",\"status\":\"ok\",\"unit\":\"\",\"rest\":\"GET\",\"ws\":\"$PLAYER_STOP_WS\"},
{\"pin\":6,\"type\": \"VolumeDown\",\"configuredAs\": \"Button\",\"status\":\"ok\",\"unit\":\"\",\"rest\":\"GET\",\"ws\":\"$VOLUME_DOWN_WS\"},
{\"pin\":7,\"type\": \"VolumeUp\",\"configuredAs\": \"Button\",\"status\":\"ok\",\"unit\":\"\",\"rest\":\"GET\",\"ws\":\"$VOLUME_UP_WS\"},
{\"pin\":8,\"type\": \"VolumeMute\",\"configuredAs\": \"Switch\",\"status\":\"$VOLUME_MUTE_STATUS\",\"unit\":\"\",\"rest\":\"GET\",\"ws\":\"$VOLUME_MUTE_WS\"}]}")


echo "$RESULT"




