#! /bin/bash

if [ -z "$1" ]; then
	sleep 10
else
	sleep $1
fi

cd '/home/pi/smartHome2/'

#run the device connector 
cd './device_connector'
echo "running device_connector..."
screen -d -m ./device-gateway

#run the subscribers
cd ..
cd './subscribers'
#for f in *.py; do screen -d -m python "$f"; done
for f in *.py; do 
	echo "running $f..."
	screen -d -m python "$f"; 
done

#run the control strategies
cd ..
cd './controlStrategies'
#for f in *.py; do screen -d -m python "$f"; done
for f in *.py; do 
	echo "running $f..."
	screen -d -m python "$f"; 
done




