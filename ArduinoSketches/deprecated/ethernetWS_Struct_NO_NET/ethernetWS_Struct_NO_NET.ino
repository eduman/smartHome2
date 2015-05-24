#include <String.h>
#include <SPI.h>
#include <Ethernet.h>
#include "ethernetWS.h"
#include "deviceType.h"

typedef struct Device{
  int pin;
  int pinState;
  boolean boolStatus;
  String type;
  String configuredAs;
} t_device;



String readString; 
String returnString;

//boolean reboot = false;
boolean configured = true;
byte moteid = 2;
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xEC };
byte ip[] = { 192, 168, 3, moteid };
byte gateway[]  = { 192, 168, 3, 1 };
byte subnet[] = { 255, 255, 255, 0 }; 
EthernetServer server(8082); 


struct Device relayAudio = {RELAY_AUDIO_PIN, LOW, false, "Audio Input", SWITCH};
struct Device relay220V = {RELAY_220V_PIN, LOW, false, "Subufer", SWITCH};
struct Device buttonAudio = {BUTTON_AUDIO_PIN, LOW, false, "Button Audio", IN};
struct Device button220V = {BUTTON_220V_PIN, LOW, false, "Button Subufer", IN};

void setup(){

  pinMode(relayAudio.pin , OUTPUT);
  pinMode(relay220V.pin , OUTPUT);
  pinMode(buttonAudio.pin, INPUT);
  pinMode(button220V.pin, INPUT);
  Serial.begin(9600);  
  Ethernet.begin(mac, ip, gateway, subnet);
  Serial.println("Started...");
} 

void loop(){ 
  buttonAudio.pinState = digitalRead(buttonAudio.pin);
  button220V.pinState = digitalRead(button220V.pin);

  if (buttonAudio.pinState == HIGH) {  
    changeRelayStatus(&relayAudio);     
    //Serial.println("Button Audio HIGH");
    delay(600);
  }

  if (button220V.pinState == HIGH) {
    changeRelayStatus(&relay220V);   
    //Serial.println("button 220 HIGH");
    delay(600);
  }
  
  
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
          if(readString.indexOf(setCommandStr + relayStr + coupleSeparator + relayAudio.pin  + andSeparator + valueStr + coupleSeparator + "0") > 0) {
            changeRelayStatus(relayAudio.pin, 0, &relayAudio.boolStatus);
          }
          // "set&relay=2&value=1" 
          else if(readString.indexOf(setCommandStr + relayStr + coupleSeparator + relayAudio.pin  + andSeparator + valueStr + coupleSeparator + "1") > 0) {
            changeRelayStatus(relayAudio.pin, 1, &relayAudio.boolStatus);
            //Serial.println("set&relay=2&value=1");
          }
          // "set&relay=3&value=0" 
          else if(readString.indexOf(setCommandStr + relayStr + coupleSeparator + relay220V.pin  + andSeparator + valueStr + coupleSeparator + "0") > 0) {
            changeRelayStatus(relay220V.pin, 0, &relay220V.boolStatus);
            //Serial.println("set&relay=3&value=0");
          }
          // "set&relay=3&value=1" 
          else if(readString.indexOf(setCommandStr + relayStr + coupleSeparator + relay220V.pin  + andSeparator + valueStr + coupleSeparator + "1") > 0) {
            changeRelayStatus(relay220V.pin , 1, &relay220V.boolStatus);
            //Serial.println("set&relay=3&value=1");
          }
          //"get&relay=2"
          else if (readString.indexOf(getCommandStr + relayStr + coupleSeparator + relayAudio.pin) > 0){
            //getRelayStatus (relayAudio.pin, successStr, relayStr, &relayAudio.boolStatus);
            getRelayStatus (&relayAudio, successStr, relayStr);
            //Serial.println("get-relay=2");
          }
          //"get&relay=3"
          else if (readString.indexOf(getCommandStr + relayStr + coupleSeparator + relay220V.pin)> 0){
            //getRelayStatus (relay220V.pin, successStr, relayStr, &relay220V.boolStatus);
            getRelayStatus (&relay220V, successStr, relayStr);
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
          } else {
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
  
  returnString.concat(deviceToString(&relayAudio));
  returnString.concat(deviceToString(&relay220V));
  returnString.concat(deviceToString(&buttonAudio));
  returnString.concat(deviceToString(&button220V));
  
}

String deviceToString (struct Device *device){
  String s;
  s.concat((*device).pin + fieldSeparator + 
            (*device).type + fieldSeparator + 
            (*device).configuredAs + fieldSeparator+ 
            (*device).boolStatus+ fieldSeparator+ rowSeparator);
  return s;
}

void changeRelayStatus(struct Device *device) {
  changeRelayStatus ( (*device).pin, !((*device).boolStatus), &((*device).boolStatus));
  return;
}

void changeRelayStatus(int pinOut, boolean value, boolean* state){
  switch (pinOut){
  /*case 1:
    returnString = "1";
    break;*/
  case 2:
    relayActuation(pinOut, value, state);
    break;
  case 3:
    relayActuation(pinOut, value, state);
    break;
  /*case 4:
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
    break;*/
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
    getPinConfiguration();
  } 
  else {
    digitalWrite(pinOut, LOW); 
    //Serial.print("OFF pin ");
    //Serial.println(pinOut);
    *state = value;
    getPinConfiguration();
  }
  return;
} 


void getRelayStatus (struct Device *device, String resultStr, String hardwareTypeStr) {
  returnString.concat(resultStr + rowSeparator);
  returnString.concat(moteStr + coupleSeparator + moteid + fieldSeparator);
  returnString.concat(hardwareTypeStr + coupleSeparator + (*device).pin + fieldSeparator);
  returnString.concat(statusStr + coupleSeparator + (*device).boolStatus + fieldSeparator);
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

