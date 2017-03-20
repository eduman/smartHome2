#! /bin/bash

name="Image_"$(date +%y-%m-%d_%H-%M-%S)
pnmFile=$name".pnm"
jpgFile=$name".jpg"
destinationFolder=$1"/"$jpgFile
#destinationFolder="/home/pi/raspberryConfig/SmartHome/DropBoxProxy/toBeSent/"$jpgFile

#RESULT=("{\"event\":\"Scanning result\",\"value\":\"Scanning the image\",\"unit\":\"\"}")
#echo "$RESULT"

>&2 /usr/bin/sudo /usr/bin/scanimage --r 200 > $pnmFile
>&2 /usr/bin/convert $pnmFile $jpgFile
>&2 /bin/rm -rf $pnmFile
>&2 /bin/mv $jpgFile $destinationFolder

