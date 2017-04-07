#! /bin/bash

if [ -z "$1" ]; then
	sleep 10
else
	sleep $1
fi

cd '/home/pi/smartHome2/'

cd './myWebServices'
for f in *.py; do 
	echo "running $f..."
	screen -S "$f" -d -m python "$f"; 
done


#run the subscribers
cd ..
cd './subscribers'
#for f in *.py; do screen -d -m python "$f"; done
for f in *.py; do 
	echo "running $f..."
	screen -S "$f" -d -m python "$f"; 
done


#run the publishers
#cd ..
#cd './publisher'
#echo "running mainPublisher.py..."
#screen -S "$f" -d -m python mainPublisher.py;

#run other services
cd ..
cd './services'
#for f in *.py; do screen -d -m python "$f"; done
#for f in *.py; do 
#	echo "running $f..."
#	screen -S "$f" -d -m python "$f"; 
#done
screen -S "AmazonDashSevice.py" -d -m sudo python "AmazonDashSevice.py";
screen -S "TelegramService.py" -d -m python "TelegramService.py";


#run the control strategies
cd ..
cd './controlStrategies'
#for f in *.py; do screen -d -m python "$f"; done
for f in *.py; do 
	echo "running $f..."
	screen -S "$f" -d -m python "$f"; 
done




