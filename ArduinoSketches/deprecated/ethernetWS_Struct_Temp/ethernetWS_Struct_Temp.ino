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

typedef struct Sensor{
  int pin;
  int pinState;
  float value;
  String type;
  String configuredAs;
} t_sensor;

String readString; 
String returnString;

//boolean reboot = false;
boolean configured = true;
byte moteid = 4;
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xE3 };
byte ip[] = { 192, 168, 3, moteid };
byte gateway[]  = { 192, 168, 3, 1 };
byte subnet[] = { 255, 255, 255, 0 }; 
EthernetServer server(8082); 


struct Sensor tempSens = {TEMPERATURE_SENSOR, LOW, false, "Air Temperature", SENSOR};

void setup(){

  Serial.begin(9600);  
  Ethernet.begin(mac, ip, gateway, subnet);
  //changeRelayStatus(&relay220V); // in case of inverted swith  
  Serial.println("Started...");
} 

void loop(){ 
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
          //getPinConfiguration
          if (readString.indexOf(getPinConfigurationStr)> 0){
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
  
  tempSens.value  = log(((10240000/analogRead(tempSens.pin)) - 10000));
  tempSens.value  = 1 / (0.001129148 + (0.000234125 * tempSens.value) + (0.0000000876741 * tempSens.value * tempSens.value * tempSens.value));
  tempSens.value  = tempSens.value - 273.15;           // Convert Kelvin to Celcius
  
  //tempSens.value = (analogRead(tempSens.pin)*5.0*10.0)/1024.0;
  returnString.concat(sensorToString(&tempSens));  
}

String deviceToString (struct Device *device){
  String s;
  s.concat((*device).pin + fieldSeparator + 
            (*device).type + fieldSeparator + 
            (*device).configuredAs + fieldSeparator+ 
            (*device).boolStatus+ fieldSeparator+ rowSeparator);
  return s;
}

String sensorToString (struct Sensor *sensor){
  String s;
  s.concat((*sensor).pin + fieldSeparator + 
            (*sensor).type + fieldSeparator + 
            (*sensor).configuredAs + fieldSeparator+ 
            floatToString((*sensor).value, 2)+ fieldSeparator+ rowSeparator);
  return s;
}

void changeRelayStatus(struct Device *device) {
  //struct Device dev = *device;
  changeRelayStatus ( (*device).pin, !((*device).boolStatus), &((*device).boolStatus));
  return;
}

void changeRelayStatus(int pinOut, boolean value, boolean* state){
  switch (pinOut){
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

