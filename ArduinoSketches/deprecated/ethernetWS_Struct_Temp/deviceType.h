#ifndef DEVICETYPE_H
#define DEVICETYPE_H

/*
The Ethernet shield allows you to connect a WizNet Ethernet 
 controller to the Arduino via the SPI bus. 
 It uses pins 10, 11, 12, and 13 for the SPI connection to the WizNet. 
 Digital pin 4 is used to control the slave select pin on the SD card.
 */

/*
From datasheet
 Available digital pins: (1,) 2, 3, 5, 6, 7, 8, 9.
 Available analogic pins: A0, A1, A2, A3, A4, A5.
 */
 
#define RELAY_AUDIO_PIN 2
#define RELAY_220V_PIN 3
#define BUTTON_AUDIO_PIN 5
#define BUTTON_220V_PIN 6
#define SAMSUNG_REMOTE_IR_PIN 7
#define APPLE_REMOTE_IR_PIN 8
#define TEMPERATURE_SENSOR 0


const String SWITCH = "switch";
const String INVERTED_SWITCH = "Iswitch";
const String IN = "input";
const String SENSOR = "sensor";

#endif
