#include <String.h>
#include <SPI.h>
#include <Ethernet.h>
//#include <EEPROM.h>
//#include "IRremote.h" // edo: radio

//#define APPLE_MENU_BUTTON 0x77E1C06D // edo: radio
//#define SAMSUNG_TV_BUTTON
//#define SAMSUNG_RED_BUTTON

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


//const boolean firstBoot = false;

// pins constant
const int relayAudioPin = 2; //output
const int relay220VPin = 3; //output
const int buttonAudioPin = 5; //input
const int button220VPin = 6; //input
//const int irReceiverPin = 7; //IR input // edo: radio

// constants
const String fieldSeparator = ";";
const String rowSeparator = "\n";
const String coupleSeparator = "=";
const String andSeparator = "&";
const String errorStr = "error";
const String successStr = "success";
const String getCommandStr = "get"+andSeparator;
const String setCommandStr = "set"+andSeparator;
const String getPinConfigurationStr = "getPinConfiguration";
const String getNetConfigurationStr = "getNetConfiguration";
const String setNetConfigurationStr = "setNetConfiguration";
const String moteStr = "mote";
const String relayStr = "relay";
const String statusStr = "status";
const String valueStr = "value";
const String commandErr = "The requested command does not exist!";

// Actuation values
boolean relayAudioStatus = false;
boolean relay220VStatus = false;

// Actuation Hardware inputs
int buttonAudioState = LOW;
int button220VState = LOW;


String readString; 
String returnString;

//boolean reboot = false;
boolean configured = true;
byte moteid = 3;
byte mac[] = { 
  0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xEC };
byte ip[] = { 
  192, 168, 3, moteid };
byte gateway[]  = { 
  192, 168, 3, 1 };
byte subnet[] = { 
  255, 255, 255, 0 }; 
EthernetServer server(8082); 

//IRrecv irrecv(irReceiverPin); // edo: radio
//decode_results resultsIR;

void setup(){

  pinMode(relayAudioPin , OUTPUT);
  pinMode(relay220VPin , OUTPUT);
  pinMode(buttonAudioPin, INPUT);
  pinMode(button220VPin, INPUT);
  //Serial.begin(9600);  
  //irrecv.enableIRIn(); // Start the receiver // edo: radio
  Ethernet.begin(mac, ip, gateway, subnet);
  changeRelayStatus(relay220VPin, !relay220VStatus, &relay220VStatus);  
  //Serial.println("Started...");
  /*
  pinMode (REDpin, OUTPUT);
   pinMode (GREENpin, OUTPUT);
   digitalWrite (REDpin, red);
   digitalWrite (GREENpin, green);
   */
} 
void loop(){ 
  buttonAudioState = digitalRead(buttonAudioPin);
  button220VState = digitalRead(button220VPin);

  if (buttonAudioState == HIGH) {  
    changeRelayStatus(relayAudioPin, !relayAudioStatus, &relayAudioStatus);    
    //Serial.println("Button Audio HIGH");
    delay(600);
  }

  if (button220VState == HIGH) {
    changeRelayStatus(relay220VPin, !relay220VStatus, &relay220VStatus);  
    //Serial.println("button 220 HIGH");
    delay(600);
  }

  /*
  // edo: radio
   if (irrecv.decode(&resultsIR)) {
   switch (resultsIR.value)
   {
   case APPLE_MENU_BUTTON:
   changeRelayStatus(relayAudioPin, !relayAudioStatus, &relayAudioStatus);
   //Serial.println(resultsIR.value, HEX);
   break; 
   }
   irrecv.resume(); // Receive the next value
   }*/


  EthernetClient client = server.available();
  if (client) {
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        readString.concat(c); //store characters to string
        //if HTTP request has ended
        if (c == '\n') {
          //Serial.print(readString);
          // "set&relay=2&value=0"
          if(readString.indexOf(setCommandStr + relayStr + coupleSeparator + relayAudioPin  + andSeparator + valueStr + coupleSeparator + "0") > 0) {
            changeRelayStatus(relayAudioPin, 0, &relayAudioStatus);
          }
          // "set&relay=2&value=1" 
          else if(readString.indexOf(setCommandStr + relayStr + coupleSeparator + relayAudioPin  + andSeparator + valueStr + coupleSeparator + "1") > 0) {
            changeRelayStatus(relayAudioPin, 1, &relayAudioStatus);
            //Serial.println("set&relay=2&value=1");
          }
          // "set&relay=3&value=0" 
          else if(readString.indexOf(setCommandStr + relayStr + coupleSeparator + relay220VPin  + andSeparator + valueStr + coupleSeparator + "0") > 0) {
            changeRelayStatus(relay220VPin, 0, &relay220VStatus);
            //Serial.println("set&relay=3&value=0");
          }
          // "set&relay=3&value=1" 
          else if(readString.indexOf(setCommandStr + relayStr + coupleSeparator + relay220VPin  + andSeparator + valueStr + coupleSeparator + "1") > 0) {
            changeRelayStatus(relay220VPin , 1, &relay220VStatus);
            //Serial.println("set&relay=3&value=1");
          }
          //"get&relay=2"
          else if (readString.indexOf(getCommandStr + relayStr + coupleSeparator + relayAudioPin) > 0){
            getRelayStatus (relayAudioPin, successStr, relayStr, &relayAudioStatus);
            //Serial.println("get-relay=2");
          }
          //"get&relay=3"
          else if (readString.indexOf(getCommandStr + relayStr + coupleSeparator + relay220VPin)> 0){
            getRelayStatus (relay220VPin, successStr, relayStr, &relay220VStatus);
            //Serial.println("get&relay=3");
          }
          //getPinConfiguration
          else if (readString.indexOf(getPinConfigurationStr)> 0){
            getPinConfiguration();
            //Serial.println("getPinConfiguration");
          }
          //getNetConfiguration
          else if (readString.indexOf(getNetConfigurationStr)> 0){
            getNetConfiguration();
            //Serial.println("getNetConfiguration");
          }
          //setNetConfiguration
          /*else if (readString.indexOf(setNetConfigurationStr)> 0){
           //setNetConfiguration&ip=192.168.1.2&gw=192.168.1.1&sb=255.255.255.0
           reboot = setNetConfiguration();
           //Serial.println("setNetConfiguration");
           }*/
          else {
            returnString.concat(successStr + rowSeparator);
            returnString.concat(commandErr);
            //Serial.println(commandErr);
          }

          client.println("HTTP/1.1 200 OK");
          client.println("Content-Type: text; charset=utf-8");
          client.println();
          client.println(returnString);
          readString="";
          delay(1);
          client.stop();

        } 
      }
    }

  } 

  returnString = "";

} 

void getNetConfiguration(){
  returnString.concat(successStr + rowSeparator);
  returnString.concat("configured" + fieldSeparator + configured + fieldSeparator + rowSeparator);
  returnString.concat("ip" + fieldSeparator + ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3] + fieldSeparator +  rowSeparator);
  returnString.concat("subnet" + fieldSeparator + subnet[0] + "." + subnet[1] + "." + subnet[2] + "." + subnet[3] + fieldSeparator + rowSeparator);
  returnString.concat("gateway" + fieldSeparator + gateway[0] + "." + gateway[1] + "." + gateway[2] + "." + gateway[3] + fieldSeparator + rowSeparator);
}

void getPinConfiguration(){
  returnString.concat(successStr + rowSeparator);
  returnString.concat("Pin" + fieldSeparator + "Type" + fieldSeparator + "ConfiguredAs" + fieldSeparator + "status" + fieldSeparator + rowSeparator);
  returnString.concat(relayAudioPin + fieldSeparator + "Relay audio" + fieldSeparator + "switch" + fieldSeparator+ relayAudioStatus+ fieldSeparator+ rowSeparator);
  returnString.concat(relay220VPin+ fieldSeparator + "Relay 220V" + fieldSeparator + "switch" + fieldSeparator+ relay220VStatus+ fieldSeparator+rowSeparator);
  returnString.concat(buttonAudioPin + fieldSeparator + "Button audio" + fieldSeparator + "input" + fieldSeparator + "0" + fieldSeparator + rowSeparator);
  returnString.concat(button220VPin + fieldSeparator + "Button 220V" + fieldSeparator+ "input" + fieldSeparator + "0" +fieldSeparator + rowSeparator);
}

void changeRelayStatus(int pinOut, boolean value, boolean* state){
  switch (pinOut){
  case 1:
    returnString = "1";
    break;
  case 2:
    relayActuation(pinOut, value, state);
    break;
  case 3:
    relayActuation(pinOut, value, state);
    break;
  case 4:
    returnString = "4";
    break;
  case 5:
    returnString = "5";
    break;
  case 6:
    returnString = "6";
    break;
  case 7:
    returnString="7";
    break;
  case 8:
    returnString = "8";
    break;
  case 9:
    returnString = "9";
    break;
  case 10:
    returnString = "10";
    break;
  case 11:
    returnString = "11";
    break;
  case 12:
    returnString = "12";
    break;
  case 13:
    returnString = "13";
    break;
  default:
    returnString = commandErr;
    break;
  }
  return;
}

void relayActuation (int pinOut, boolean value, boolean *state){
  if (value){
    digitalWrite(pinOut, HIGH); 
    //Serial.print("ON pin ");
    //Serial.println(pinOut);
    *state = value;
    //getRelayStatus (pinOut, successStr, relayStr, state);
    getPinConfiguration();
  } 
  else {
    digitalWrite(pinOut, LOW); 
    //Serial.print("OFF pin ");
    //Serial.println(pinOut);
    *state = value;
    //getRelayStatus (pinOut, successStr, relayStr, state);
    getPinConfiguration();
  }
  return;
} 


void getRelayStatus (int pinOut, String resultStr, String hardwareTypeStr, boolean *state) {
  returnString.concat(resultStr + rowSeparator);
  returnString.concat(moteStr + coupleSeparator + moteid + fieldSeparator);
  returnString.concat(hardwareTypeStr + coupleSeparator + pinOut + fieldSeparator);
  returnString.concat(statusStr + coupleSeparator + *(state) + fieldSeparator);
  return;
}

String floatToString(float number, uint8_t digits) 
{ 
  String resultString = "";
  // Handle negative numbers
  if (number < 0.0)
  {
    resultString += "-";
    number = -number;
  }

  // Round correctly so that print(1.999, 2) prints as "2.00"
  float rounding = 0.5;
  for (uint8_t i=0; i<digits; ++i)
    rounding /= 10.0;

  number += rounding;

  // Extract the integer part of the number and print it
  unsigned long int_part = (unsigned long)number;
  float remainder = number - (float)int_part;
  resultString += int_part;

  // Print the decimal point, but only if there are digits beyond
  if (digits > 0)
    resultString += "."; 

  // Extract digits from the remainder one at a time
  while (digits-- > 0)
  {
    remainder *= 10.0;
    int toPrint = int(remainder);
    resultString += toPrint;
    remainder -= toPrint; 
  } 
  return resultString;
}

