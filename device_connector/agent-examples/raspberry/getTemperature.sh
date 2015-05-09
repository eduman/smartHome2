#! /bin/bash

TEMP=$(/opt/vc/bin/vcgencmd measure_temp| egrep "[0-9.]{4,}" -o)
RESULT=("{\"event\":\"SoC Temperature\",\"value\":\"$TEMP\",\"unit\":\"°C\"}")
echo "$RESULT"
