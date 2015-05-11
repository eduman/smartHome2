
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
} t_device;

typedef struct Sensor{
  int pin;
  int code;
  float valueStatus;
  String type;
  String configuredAs;
} t_sensor;


String readString; 
//String returnString;

//boolean reboot = false;
boolean configured = true;

//TODO: change MAC and IP Address
byte moteid = 3;
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xE3 };
byte ip[] = { 192, 168, 3, moteid };
byte gateway[]  = { 192, 168, 3, 1 };
byte subnet[] = { 255, 255, 255, 0 }; 
EthernetServer server(8082); 
IPAddress remoteServer(192,168,3,254); 

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



struct Device relay1 = {RELAY_1_PIN, LOW, false, "Piantana", SWITCH}; //lampada comodino
struct Device relay2 = {RELAY_2_PIN, LOW, false, "Lampada mensola", SWITCH}; //lampada scrivania
struct Device keyboard = {KYEBOARD_ANALOG_INPUT, LOW, false, "Keyboard", IN};
struct Device motion = {MOTION_SENSOR, LOW, false, "Motion sensor", IN};
boolean previousMotionStatus = false;

struct Sensor temp = {DHT22_SENSOR, TEMPERATURE_CODE, 0.0, "Temperature", SENSOR};
struct Sensor hum = {DHT22_SENSOR, HUMIDITY_CODE, 0.0, "Humidity", SENSOR};

DHT22 myDHT22(DHT22_SENSOR);


void setup(){
  pinMode(relay1.pin , OUTPUT);
  pinMode(relay2.pin , OUTPUT);
  Serial.begin(9600);  
  Ethernet.begin(mac, ip, gateway, subnet);
  Serial.println("Started...");
} 

void loop(){
  currentMills = millis(); 
  adc_key_in = analogRead(KYEBOARD_ANALOG_INPUT);
  key = get_key(adc_key_in);
  if (key >=0 && currentMills-previousKeyBoardMills > keyboardDelay){
    previousKeyBoardMills = currentMills;
    
    switch(key) {
      case 0:
        Serial.println("S1 OK");  
        break; 
      
      case 1:
        Serial.println("S2 OK");
        break;
   
      case 2:
        Serial.println("S3 OK");
        changeRelayStatus(&relay1);
        break;
      case 3:
        Serial.println("S4 OK");
        changeRelayStatus(&relay2);
        break;      
      case 4:
        Serial.println("S5 OK");
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
          //"get&relay=2"
          else if (readString.indexOf(getCommandStr + relayStr + coupleSeparator + relay1.pin) > 0){
            //getRelayStatus (relayAudio.pin, successStr, relayStr, &relayAudio.boolStatus);
            getRelayStatus (&relay1, successStr, relayStr, &client);
            //Serial.println("get-relay=2");
          }
          //"get&relay=3"
          else if (readString.indexOf(getCommandStr + relayStr + coupleSeparator + relay2.pin)> 0){
            //getRelayStatus (relay220V.pin, successStr, relayStr, &relay220V.boolStatus);
            getRelayStatus (&relay2, successStr, relayStr, &client);
            //Serial.println("get&relay=3");
          }
          //getPinConfiguration
          else if (readString.indexOf(getPinConfigurationStr)> 0){
            getPinConfiguration(&client);
            //Serial.println("getPinConfiguration");
          }
          //getNetConfiguration
          else if (readString.indexOf(getNetConfigurationStr)> 0){
            getNetConfiguration(&client);
            //Serial.println("getNetConfiguration");
          } else {
            client.println("HTTP/1.1 200 OK");
            client.println("Content-Type: text; charset=utf-8");
            client.println();
            client.print(successStr + rowSeparator);
            client.println(commandErr);
            delay(1);
            client.stop(); 
          }

          /*client.println("HTTP/1.1 200 OK");
          client.println("Content-Type: text; charset=utf-8");
          client.println();
          client.println(returnString);*/
          readString="";
          //delay(1);
          //client.stop();

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

void getNetConfiguration(EthernetClient *client){
  
  (*client).println("HTTP/1.1 200 OK");
  (*client).println("Content-Type: text; charset=utf-8");
  (*client).println();
  
  (*client).print(successStr + rowSeparator);
  (*client).print("configured" + fieldSeparator + configured + fieldSeparator + rowSeparator);
  (*client).print("ip" + fieldSeparator + ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3] + fieldSeparator +  rowSeparator);
  (*client).print("subnet" + fieldSeparator + subnet[0] + "." + subnet[1] + "." + subnet[2] + "." + subnet[3] + fieldSeparator + rowSeparator);
  (*client).print("gateway" + fieldSeparator + gateway[0] + "." + gateway[1] + "." + gateway[2] + "." + gateway[3] + fieldSeparator + rowSeparator);
  
  delay(1);
  (*client).stop(); 
 }

void getPinConfiguration(EthernetClient *client){
  updateDHTInfo();
  
  (*client).println("HTTP/1.1 200 OK");
  (*client).println("Content-Type: text; charset=utf-8");
  (*client).println();
  
  (*client).print(successStr + rowSeparator);
  (*client).print("Pin" + fieldSeparator + "Type" + fieldSeparator + "ConfiguredAs" + fieldSeparator + "status" + fieldSeparator + rowSeparator);
  
  deviceToString(&relay1, client);
  deviceToString(&relay2, client);
  deviceToString(&keyboard, client);
  deviceToString(&motion, client);
  sensorToString(&temp, client);
  sensorToString(&hum, client);
  
  delay(1);
  (*client).stop();  
}

void deviceToString (struct Device *device, EthernetClient *client){
  (*client).print((*device).pin + fieldSeparator + 
            (*device).type + fieldSeparator + 
            (*device).configuredAs + fieldSeparator+ 
            (*device).boolStatus+ fieldSeparator+ rowSeparator);
  return;
}

void sensorToString (struct Sensor *sensor, EthernetClient *client){
  switch ((*sensor).code){
    case TEMPERATURE_CODE:
      (*client).print((*sensor).pin + fieldSeparator + 
            (*sensor).type + fieldSeparator + 
            (*sensor).configuredAs + fieldSeparator+ 
            floatToString((*sensor).valueStatus, 2) + "C"+ fieldSeparator+ rowSeparator);
      break;
    case HUMIDITY_CODE:
      (*client).print((*sensor).pin + fieldSeparator + 
            (*sensor).type + fieldSeparator + 
            (*sensor).configuredAs + fieldSeparator+ 
            floatToString((*sensor).valueStatus, 2) + "%" + fieldSeparator+ rowSeparator);
      break;
    default:
      (*client).print((*sensor).pin + fieldSeparator + 
            (*sensor).type + fieldSeparator + 
            (*sensor).configuredAs + fieldSeparator+ 
            floatToString((*sensor).valueStatus, 2) + " err" + fieldSeparator+ rowSeparator);
       break;
  }

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
  getPinConfiguration(client);
  return;
}

void changeRelayStatus(struct Device *device) {
  //changeRelayStatus ( (*device).pin, !((*device).boolStatus), &((*device).boolStatus), client);
  relayActuation( (*device).pin, !((*device).boolStatus), &((*device).boolStatus));
  return;
}

void changeRelayStatus(int pinOut, boolean value, boolean* state, EthernetClient *client){
  relayActuation(pinOut, value, state);
  getPinConfiguration(client);
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


void getRelayStatus (struct Device *device, String resultStr, String hardwareTypeStr, EthernetClient *client) {
  (*client).println("HTTP/1.1 200 OK");
  (*client).println("Content-Type: text; charset=utf-8");
  (*client).println();
  
  (*client).print(resultStr + rowSeparator);
  (*client).print(moteStr + coupleSeparator + moteid + fieldSeparator);
  (*client).print(hardwareTypeStr + coupleSeparator + (*device).pin + fieldSeparator);
  (*client).print(statusStr + coupleSeparator + (*device).boolStatus + fieldSeparator);
  
  delay(1);
  (*client).stop();
   
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

