
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
byte moteid = 3;
char ipStr[] = "192.168.1.3";

byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xE3 };
byte ip[] = { 192, 168, 1, moteid };


byte gateway[]  = { 192, 168, 3, 1 };
char gatewayStr[] = "192.168.3.1";

byte subnet[] = { 255, 255, 255, 0 }; 
char subnetStr[] = "255.255.255.0";

//String ipStr =  String (ip[0]) + "." + String (ip[1]) + "." + String (ip[2]) + "." + String (ip[3]);
//String gatewayStr = String (gateway[0]) + "." + String (gateway[1]) + "." + String (gateway[2]) + "." + String (gateway[3]);
//String subnetStr = String (subnet[0]) + "." + String (subnet[1]) + "." + String (subnet[2]) + "." + String (subnet[3]);

char portStr[] = "8082";

EthernetServer server(8082); 
IPAddress remoteServer(192,168,1,254); 

char descriptionStr [] = "Arduino device";
char typeStr [] = "arduino";

int adc_key_in;
int key=-1;
int oldkey=-1;

const unsigned int keyboardDelay = 600;
const unsigned int motionDelay = 100;
const unsigned long motionPeriodicEvent = 60000; // 1 minute //300000; 5 minutes

unsigned long currentMills = 0;
unsigned long previousKeyBoardMills = 0;
unsigned long previousMotionMills = 0;
unsigned long previousPeriodicMotionEventMills = 0;


struct Device relay1 = {RELAY_1_PIN, LOW, false, "Piantana", SWITCH, ""}; //mensola
struct Device relay2 = {RELAY_2_PIN, LOW, false, "Lampada mensola", SWITCH, ""}; //mensola

//struct Device relay1 = {RELAY_1_PIN, LOW, false, "Lampada scrivania", SWITCH, ""}; // scrivania
//struct Device relay2 = {RELAY_2_PIN, LOW, false, "Televisione", SWITCH, ""}; //Televisione

struct Device keyboard = {KYEBOARD_ANALOG_INPUT, LOW, false, "Keyboard", IN, ""};
struct Device motion = {MOTION_SENSOR, LOW, false, "Motion sensor", IN, ""};
boolean previousMotionStatus = false;

struct Sensor temp = {DHT22_SENSOR, TEMPERATURE_CODE, 0.0, "Temperature", SENSOR, "C"};
struct Sensor hum = {DHT22_SENSOR, HUMIDITY_CODE, 0.0, "Humidity", SENSOR, "%"};

DHT22 myDHT22(DHT22_SENSOR);


void setup(){
  pinMode(relay1.pin , OUTPUT);
  pinMode(relay2.pin , OUTPUT);
  //Serial.begin(9600);  
  Ethernet.begin(mac, ip, gateway, subnet);
  //Serial.println("Started...");
} 

void loop(){
  currentMills = millis(); 
  adc_key_in = analogRead(KYEBOARD_ANALOG_INPUT);
  key = get_key(adc_key_in);
  if (key >0 && currentMills-previousKeyBoardMills > keyboardDelay){
    previousKeyBoardMills = currentMills;
    
    switch(key) {
      case 0:
        // never enabled !! 
        //Serial.println("S1 OK");  
        break; 
      
      case 1:
        //Serial.println("S2 OK");
        break;
   
      case 2:
        //Serial.println("S3 OK");
        break;
      case 3:
        //Serial.println("S4 OK");
        changeRelayStatus(&relay2);
        break;      
      case 4:
        //Serial.println("S5 OK");
        changeRelayStatus(&relay1);  
        break;  
    }
    //delay(keyboardDelay);  // wait for debounce time
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
      sendData("/ArduinoProxy?moteID=" + s + "&measureType=Motion&measureValue=true");
    } else {
      sendData("/ArduinoProxy?moteID=" + s + "&measureType=Motion&measureValue=false");
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
      sendData("/ArduinoProxy?moteID=" + s + "&measureType=Motion&measureValue=true");
    } else {
      sendData("/ArduinoProxy?moteID=" + s + "&measureType=Motion&measureValue=false");
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
  if (sendClient.connect(remoteServer, 8082)) {
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
  updateDHTInfo();
  
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
  deviceToStringNoWS(&keyboard, client);
  (*client).print(",");
  deviceToStringNoWS(&motion, client);
  (*client).print(",");
  sensorToString(&temp, client);
  (*client).print(",");
  sensorToString(&hum, client);
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


void updateDHTInfo() {
  //TODO: clean the code
    DHT22_ERROR_t errorCode;
    errorCode = myDHT22.readData();
    //Serial.println(errorCode);
    switch(errorCode) {
      case DHT_ERROR_NONE:
          /*Serial.print("Got Data ");
          Serial.print(myDHT22.getTemperatureC());
          Serial.print("C ");
          Serial.print(myDHT22.getHumidity());
          Serial.println("%");
          // Alternately, with integer formatting which is clumsier but more compact to store and
  	// can be compared reliably for equality:
  	//	  
          char buf[128];
          sprintf(buf, "Integer-only reading: Temperature %hi.%01hi C, Humidity %i.%01i %% RH",
                       myDHT22.getTemperatureCInt()/10, abs(myDHT22.getTemperatureCInt()%10),
                       myDHT22.getHumidityInt()/10, myDHT22.getHumidityInt()%10);
          Serial.println(buf);*/
        
        temp.valueStatus = myDHT22.getTemperatureC();
        hum.valueStatus = myDHT22.getHumidity();
        
        break;
      /*case DHT_ERROR_CHECKSUM:
          Serial.print("check sum error ");
          Serial.print(myDHT22.getTemperatureC());
          Serial.print("C ");
          Serial.print(myDHT22.getHumidity());
          Serial.println("%");
        break;
      case DHT_BUS_HUNG:
        Serial.println("BUS Hung ");
        break;
      case DHT_ERROR_NOT_PRESENT:
        Serial.println("Not Present ");
        break;
      case DHT_ERROR_ACK_TOO_LONG:
        Serial.println("ACK time out ");
        break;
      case DHT_ERROR_SYNC_TIMEOUT:
        Serial.println("Sync Timeout ");
        break;
      case DHT_ERROR_DATA_TIMEOUT:
        Serial.println("Data Timeout ");
        break;
      case DHT_ERROR_TOOQUICK:
        Serial.println("Polled to quick ");
        break;*/
    }
  return;
}

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

