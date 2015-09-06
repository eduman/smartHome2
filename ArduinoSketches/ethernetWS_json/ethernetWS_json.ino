
/*
Fork of ethernetWS_Struct_NO_NET
*/

#include <String.h>
#include <SPI.h>
#include <Ethernet.h>
#include "DHT22.h"
#include "ethernetWS.h"
#include "deviceType.h"

typedef struct Device{
  int pin;
  int pinState;
  boolean boolStatus;
  String type;
  String configuredAs;
  String unit;
} t_device;

typedef struct Sensor{
  int pin;
  int code;
  float valueStatus;
  String type;
  String configuredAs;
  String unit;
} t_sensor;


String readString; 
//String returnString;

//boolean reboot = false;
boolean configured = true;

//TODO: change MAC and IP Address
byte moteid = 2;
char ipStr[] = "192.168.1.2";

byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xE2 };
byte ip[] = { 192, 168, 1, moteid };


byte gateway[]  = { 192, 168, 3, 1 };
char gatewayStr[] = "192.168.3.1";

byte subnet[] = { 255, 255, 255, 0 }; 
char subnetStr[] = "255.255.255.0";

char portStr[] = "8082";

EthernetServer server(8082); 
IPAddress remoteServer(192,168,3,254); 

char descriptionStr [] = "Arduino device";
char typeStr [] = "arduino";

const unsigned int buttonDelay = 600;
const unsigned int motionDelay = 100;
const unsigned long motionPeriodicEvent = 60000; // 1 minute //300000; 5 minutes

unsigned long currentMills = 0;
unsigned long previousButtonAudioMills = 0;
unsigned long previousButton220VMills = 0;

unsigned long previousMotionMills = 0;
unsigned long previousPeriodicMotionEventMills = 0;


struct Device relay1 = {RELAY_AUDIO_PIN, LOW, false, "Audio Input", SWITCH, ""}; //relay audio
struct Device relay2 = {RELAY_220V_PIN, LOW, false, "Subufer", SWITCH, ""}; // relay 220V
struct Device buttonAudio = {BUTTON_AUDIO_PIN, LOW, false, "Button Audio", IN, ""};
struct Device button220V = {BUTTON_220V_PIN, LOW, false, "Button Subufer", IN, ""};
//struct Device motion = {MOTION_SENSOR, LOW, false, "Motion sensor", IN, ""};
boolean previousMotionStatus = false;



void setup(){
  pinMode(relay1.pin , OUTPUT);
  pinMode(relay2.pin , OUTPUT);
  pinMode(buttonAudio.pin, INPUT);
  pinMode(button220V.pin, INPUT);
  //Serial.begin(9600);  
  Ethernet.begin(mac, ip, gateway, subnet);
  //Serial.println("Started...");
} 

void loop(){  
  buttonAudio.pinState = digitalRead(buttonAudio.pin);
  button220V.pinState = digitalRead(button220V.pin);
  currentMills = millis(); 

  if (buttonAudio.pinState == HIGH && currentMills-previousButtonAudioMills > buttonDelay) {
    previousButtonAudioMills = currentMills;  
    changeRelayStatus(&relay1);     
    //Serial.println("Button Audio HIGH");
  }

  if (button220V.pinState == HIGH && currentMills-previousButton220VMills > buttonDelay) {
    previousButton220VMills = currentMills;  
    changeRelayStatus(&relay2);   
    //Serial.println("button 220 HIGH");
  }
  
  
  //TODO ABILITARE
  /*currentMills = millis(); 
  if (currentMills-previousMotionMills > motionDelay) {
    previousMotionMills = currentMills;
    motion.boolStatus = digitalRead(motion.pin);
    //Serial.print("motion ");
    //Serial.println (motion.boolStatus);
    if (motion.boolStatus != previousMotionStatus){
      previousMotionStatus = motion.boolStatus;
      // send event
      //Serial.print("Send motion event ");
      //Serial.println(motion.boolStatus);
      String s= "";
      s.concat (moteid);
      if (motion.boolStatus) {
      sendData("/rest/arduino/publisher?deviceID" + s + "&measureType=Motion&measureValue=True");
    } else {
      sendData("/rest/arduino/publisher?deviceID" + s + "&measureType=Motion&measureValue=False");
    }
    }
  }
  
  currentMills = millis();
  if (currentMills-previousPeriodicMotionEventMills > motionPeriodicEvent) {
    previousPeriodicMotionEventMills = currentMills;
    motion.boolStatus = digitalRead(motion.pin);
    previousMotionStatus = motion.boolStatus;
    //send periodic event
    //Serial.print("Send PERIODIC motion event ");
    //Serial.println(motion.boolStatus);
    String s = "";
    s.concat (moteid);
    if (motion.boolStatus) {
      sendData("/rest/arduino/publisher?deviceID" + s + "&measureType=Motion&measureValue=True");
    } else {
      sendData("/rest/arduino/publisher?deviceID" + s + "&measureType=Motion&measureValue=False");
    }
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
          if(readString.indexOf(setCommandStr + relayStr + coupleSeparator + relay1.pin  + andSeparator + valueStr + coupleSeparator + "0") > 0) {
            changeRelayStatus(relay1.pin, 0, &relay1.boolStatus, &client);
          }
          // "set&relay=2&value=1" 
          else if(readString.indexOf(setCommandStr + relayStr + coupleSeparator + relay1.pin  + andSeparator + valueStr + coupleSeparator + "1") > 0) {
            changeRelayStatus(relay1.pin, 1, &relay1.boolStatus, &client);
            //Serial.println("set&relay=2&value=1");
          }
          // "set&relay=3&value=0" 
          else if(readString.indexOf(setCommandStr + relayStr + coupleSeparator + relay2.pin  + andSeparator + valueStr + coupleSeparator + "0") > 0) {
            changeRelayStatus(relay2.pin, 0, &relay2.boolStatus, &client);
            //Serial.println("set&relay=3&value=0");
          }
          // "set&relay=3&value=1" 
          else if(readString.indexOf(setCommandStr + relayStr + coupleSeparator + relay2.pin  + andSeparator + valueStr + coupleSeparator + "1") > 0) {
            changeRelayStatus(relay2.pin , 1, &relay2.boolStatus, &client);
            //Serial.println("set&relay=3&value=1");
          }
          //getPinConfiguration
          else if (readString.indexOf(getPinConfigurationStr)> 0){
            getPinConfiguration(&client, false);
            //Serial.println("getPinConfiguration");
          }
          //getNetConfiguration
          else if (readString.indexOf(getNetConfigurationStr)> 0){
            getPinConfiguration(&client, false);
            //getNetConfiguration(&client);
            //Serial.println("getNetConfiguration");
          } else {
            getPinConfiguration(&client, true);
          }
          readString="";
        } 
      }
    }

  } 
} 

void sendData(String url){
  EthernetClient sendClient;
  if (sendClient.connect(remoteServer, 8084)) {
    //Serial.println("connected");
    // Make a HTTP request:
    sendClient.println("GET " + url + " HTTP/1.1");
    sendClient.println("Host: 192.168.3.254");
    sendClient.println("Connection: close");
    sendClient.println();
  } /*else {
    // kf you didn't get a connection to the server:
    Serial.println("connection failed");
  }*/
  
  /*if (sendClient.available()) {
    char c = sendClient.read();
    Serial.print(c);
  }*/
  
  // if the server's disconnected, stop the client:
  if (!sendClient.connected()) {
    //Serial.println();
    //Serial.println("disconnecting.");
    sendClient.stop();
  }
}



void getPinConfiguration(EthernetClient *client, boolean isError){
  //updateDHTInfo();
  
  (*client).println("HTTP/1.1 200 OK");
  (*client).println("Access-Control-Allow-Origin:*");
  (*client).println("Connection:keep-alive");
  (*client).println("Content-Type:application/json");
  (*client).println("Server: Arduino");
  //(*client).println("Transfer-Encoding:chunked");
  
  /*(*client).println("Content-Type: application/json;charset=utf-8");
  (*client).println("Server: Arduino");
  (*client).println("Connnection: close");*/  
  (*client).println();
    
  (*client).print("{");
  //(*client).print("\"configured\":" + String (configured) + ",");
  if (configured)
    (*client).print("\"configured\":true,");
  else 
    (*client).print("\"configured\":false,");
  
  
  //Printing the ip address
  (*client).print("\"ip\":\"");
  (*client).print(ipStr);
  (*client).print("\",");
  
  //Printing the subnet mask
  (*client).print("\"subnet\":\"");
  (*client).print(subnetStr);
  (*client).print("\",");
  
  //Printing the gateway
  (*client).print("\"gateway\":\"");
  (*client).print(gatewayStr);
  (*client).print("\",");
  
  //Printing the port
  (*client).print("\"port\":\"");
  (*client).print(portStr);
  (*client).print("\",");
  
  //Printing the description
  (*client).print("\"description\":\"");
  (*client).print(descriptionStr);
  (*client).print("\",");

  //Printing the type
  (*client).print("\"type\":\"");
  (*client).print(typeStr);
  (*client).print("\",");
  
  //(*client).print("\"isError\":" + String (isError) + ",");
  if (!isError)
    (*client).print("\"isError\":true,");
  else 
    (*client).print("\"isError\":false,");
    
  (*client).print("\"functions\":[");
  
  deviceToString(&relay1, client);
  (*client).print(",");
  deviceToString(&relay2, client);
  (*client).print(",");
  deviceToStringNoWS(&buttonAudio, client);
  (*client).print(",");
  deviceToStringNoWS(&button220V, client);
  /*(*client).print(",");
  deviceToString(&motion, client); */ 
  (*client).print("]}");
  
  delay(1);
  (*client).stop();  
}

void deviceToString (struct Device *device, EthernetClient *client){
  (*client).print("{\"pin\":" + String ((*device).pin) + ","); 
  (*client).print("\"type\":\"" + (*device).type  + "\",");
  (*client).print("\"configuredAs\":\"" +  (*device).configuredAs  + "\",");
  (*client).print("\"status\":\"" +  String ((*device).boolStatus)  + "\",");
  (*client).print("\"unit\":\"" +  (*device).unit  + "\",");
  (*client).print("\"rest\":\"GET\",");
  (*client).print("\"ws\":\"http://"); 
  (*client).print(ipStr);
  (*client).print(":");
  (*client).print(portStr); 
  (*client).print("/set&relay=" + String ((*device).pin) + "&value=" + String (!(*device).boolStatus)+"\"}");
  return;
}

void deviceToStringNoWS (struct Device *device, EthernetClient *client){
  (*client).print("{\"pin\":" + String ((*device).pin) + ","); 
  (*client).print("\"type\":\"" + (*device).type  + "\",");
  (*client).print("\"configuredAs\":\"" +  (*device).configuredAs  + "\",");
  (*client).print("\"status\":\"" +  String ((*device).boolStatus)  + "\",");
  (*client).print("\"unit\":\"" +  (*device).unit  + "\",");
  (*client).print("\"rest\":\"\",");
  (*client).print("\"ws\":\"\"}");
  return;
}

void sensorToString (struct Sensor *sensor, EthernetClient *client){
  (*client).print("{\"pin\":" + String ((*sensor).pin) + ","); 
  (*client).print("\"type\":\"" + (*sensor).type  + "\",");
  (*client).print("\"configuredAs\":\"" +  (*sensor).configuredAs  + "\",");
  (*client).print("\"status\":\"" +  floatToString((*sensor).valueStatus, 2) + "\",");
  (*client).print("\"unit\":\"" +  (*sensor).unit  + "\",");
  (*client).print("\"rest\":\"GET\",");
  (*client).print("\"ws\":\"http://");
  (*client).print(ipStr); 
  (*client).print(":");
  (*client).print(portStr);
  (*client).print("/getconfiguration\"}");
  return;
}


/*void updateDHTInfo() {
  //TODO: clean the code
    DHT22_ERROR_t errorCode;
    errorCode = myDHT22.readData();
    //Serial.println(errorCode);
    switch(errorCode) {
      case DHT_ERROR_NONE:
        temp.valueStatus = myDHT22.getTemperatureC();
        hum.valueStatus = myDHT22.getHumidity();
        break;
    }
  return;
}*/

void changeRelayStatus(struct Device *device, EthernetClient *client) {
  //changeRelayStatus ( (*device).pin, !((*device).boolStatus), &((*device).boolStatus), client);
  relayActuation( (*device).pin, !((*device).boolStatus), &((*device).boolStatus));
  getPinConfiguration(client, false);
  return;
}

void changeRelayStatus(struct Device *device) {
  //changeRelayStatus ( (*device).pin, !((*device).boolStatus), &((*device).boolStatus), client);
  relayActuation( (*device).pin, !((*device).boolStatus), &((*device).boolStatus));
  return;
}

void changeRelayStatus(int pinOut, boolean value, boolean* state, EthernetClient *client){
  relayActuation(pinOut, value, state);
  getPinConfiguration(client, false);
  return;
}

void relayActuation (int pinOut, boolean value, boolean *state){
  if (value){
    digitalWrite(pinOut, HIGH); 
    *state = value;
  } 
  else {
    digitalWrite(pinOut, LOW); 
    *state = value;
  }
  return;
} 

// Convert ADC value to key number
int get_key(unsigned int input){
  int k;
  for (k = 0; k < NUM_KEYS; k++){
    if (input < adc_key_val[k]){ return k; }
  }
  if (k >= NUM_KEYS) k = -1;  // No valid key pressed
  return k;
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

