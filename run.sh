#! /bin/bash

cd '/home/pi/smartHome2/'

#run the device connector 
cd './device_connector'
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




