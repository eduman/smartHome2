
/*
Fork of ethernetWS_Struct_NO_NET
*/

#include <String.h>
#include <SPI.h>
#include <Ethernet.h>
#include "ethernetWS.h"
//#include "deviceType.h"
#include "IRremote.h"
#include "SamsungTV.h"
#define SAMSUNG_COMMANDS 20
#define MAX_RESPONSE_LENGHT 6

typedef struct cs {
  String name;
  unsigned int *raw;
} CommandStruct;


const String BUTTON = "button";
String readString; 
IRsend irsend;
String response[MAX_RESPONSE_LENGHT];

CommandStruct commands [SAMSUNG_COMMANDS];


boolean configured = true;
byte moteid = 5;
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xE3 };
byte ip[] = { 192, 168, 3, moteid };
byte gateway[]  = { 192, 168, 3, 1 };
byte subnet[] = { 255, 255, 255, 0 }; 
EthernetServer server(8082); 

void setup(){
  Serial.begin(9600);  
  Ethernet.begin(mac, ip, gateway, subnet);
  initCommands();
  Serial.println("Started...");
} 

void loop(){

  EthernetClient client = server.available();
  if (client) {
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        readString.concat(c); 
        if (c == '\n') {
          Serial.println(readString);
          // "set&relay="
          if(readString.indexOf(setCommandStr + relayStr + coupleSeparator ) > 0) {
            changeChannel(&client, &readString);
            //Serial.println("changeChannel");
          }
          //getPinConfiguration
          else if (readString.indexOf(getPinConfigurationStr) > 0){
            Serial.println("getPinConfiguration");
            getPinConfiguration(&client);
          }
          //getNetConfiguration
          else if (readString.indexOf(getNetConfigurationStr)> 0){
            Serial.println("getNetConfiguration");
            response[0] = "";
            response[0].concat(successStr + rowSeparator);
            response[0].concat("configured" + fieldSeparator + configured + fieldSeparator + rowSeparator);
            response[0].concat("ip" + fieldSeparator + ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3] + fieldSeparator +  rowSeparator);
            response[0].concat("subnet" + fieldSeparator + subnet[0] + "." + subnet[1] + "." + subnet[2] + "." + subnet[3] + fieldSeparator + rowSeparator);
            response[0].concat("gateway" + fieldSeparator + gateway[0] + "." + gateway[1] + "." + gateway[2] + "." + gateway[3] + fieldSeparator + rowSeparator);
            sendGenericHttpResponse(&client, response, 1);
          } else {
            Serial.println("error");
            response[0] = successStr + rowSeparator + commandErr;
            sendGenericHttpResponse(&client, response, 1);
            Serial.println(response[0]);
          }
          readString="";
          /*client.println("HTTP/1.1 200 OK");
          client.println("Content-Type: text; charset=utf-8");
          client.println();
          client.println(returnString);
          delay(1);
          client.stop();*/


        } 
      }
    }

  } 
} 

  void sendGenericHttpResponse (EthernetClient *client, String msg[], int lenght ){
    (*client).println("HTTP/1.1 200 OK");
    (*client).println("Content-Type: text; charset=utf-8");
    (*client).println();
  
    for (int i = 0; i < lenght; i ++){
      Serial.println(msg[i]);
      (*client).print(msg[i]);
    }
    (*client).println("ok");

  
    //(*client).print(successStr + rowSeparator);
    //(*client).print(msg);
    delay(1);
    (*client).stop();
    return;
}

/*void getNetConfiguration(EthernetClient *client){
  String response = "";
  (*client).println("HTTP/1.1 200 OK");
  (*client).println("Content-Type: text; charset=utf-8");
  (*client).println();
  (*client).print(successStr + rowSeparator);
  (*client).print("configured" + fieldSeparator + configured + fieldSeparator + rowSeparator);
  (*client).print("ip" + fieldSeparator + ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3] + fieldSeparator +  rowSeparator);
  (*client).print("subnet" + fieldSeparator + subnet[0] + "." + subnet[1] + "." + subnet[2] + "." + subnet[3] + fieldSeparator + rowSeparator);
  (*client).println("gateway" + fieldSeparator + gateway[0] + "." + gateway[1] + "." + gateway[2] + "." + gateway[3] + fieldSeparator + rowSeparator);
  
  delay(1);
  (*client).stop();
  return;
}*/

void getPinConfiguration(EthernetClient *client){
  int rIndex = 0, fakePin = 0;
  for (rIndex = 0; rIndex < MAX_RESPONSE_LENGHT; rIndex++){
    response[rIndex]= "";
  }
  
  //rIndex = 0;
  rIndex = 0;
  response[rIndex].concat(successStr + rowSeparator);
  response[rIndex].concat("Pin" + fieldSeparator + "Type" + fieldSeparator + "ConfiguredAs" + fieldSeparator + "status" + fieldSeparator + rowSeparator);
  Serial.println("metodo getPinConfiguration");
  
  rIndex = 1;
  fakePin = 0;
  //rIndex = 1;
  for (; fakePin < 4; fakePin++){
    //response[rIndex].concat( (100+fakePin) + fieldSeparator + commands[fakePin].name + fieldSeparator + BUTTON + fieldSeparator+ "false" + fieldSeparator+ rowSeparator);
    response[rIndex] += (100+fakePin) + fieldSeparator + commands[fakePin].name + fieldSeparator + BUTTON + fieldSeparator+ "false" + fieldSeparator+ rowSeparator;
  }
  rIndex++;
  
  //rIndex = 2;
  for (; fakePin < 8; fakePin++){
    //response[rIndex].concat( (100+fakePin) + fieldSeparator + commands[fakePin].name + fieldSeparator + BUTTON + fieldSeparator+ "false" + fieldSeparator+ rowSeparator);
    response[rIndex] += (100+fakePin) + fieldSeparator + commands[fakePin].name + fieldSeparator + BUTTON + fieldSeparator+ "false" + fieldSeparator+ rowSeparator;
  }
  rIndex++;
  
  //rIndex = 3;
  for (; fakePin < 12; fakePin++){
    //response[rIndex].concat( (100+fakePin) + fieldSeparator + commands[fakePin].name + fieldSeparator + BUTTON + fieldSeparator+ "false" + fieldSeparator+ rowSeparator);
    response[rIndex] += (100+fakePin) + fieldSeparator + commands[fakePin].name + fieldSeparator + BUTTON + fieldSeparator+ "false" + fieldSeparator+ rowSeparator;  
  }
  rIndex++;
  
  //rIndex = 4;
  for (; fakePin < 16; fakePin++){
    //response[rIndex].concat( (100+fakePin) + fieldSeparator + commands[fakePin].name + fieldSeparator + BUTTON + fieldSeparator+ "false" + fieldSeparator+ rowSeparator);
    response[rIndex] += (100+fakePin) + fieldSeparator + commands[fakePin].name + fieldSeparator + BUTTON + fieldSeparator+ "false" + fieldSeparator+ rowSeparator;
  }
  rIndex++;
  
  //rIndex = 5;
  for (; fakePin < 20; fakePin++){
    //response[rIndex].concat( (100+fakePin) + fieldSeparator + commands[fakePin].name + fieldSeparator + BUTTON + fieldSeparator+ "false" + fieldSeparator+ rowSeparator);
    response[rIndex] += (100+fakePin) + fieldSeparator + commands[fakePin].name + fieldSeparator + BUTTON + fieldSeparator+ "false" + fieldSeparator+ rowSeparator;
  }
  sendGenericHttpResponse(client, response, MAX_RESPONSE_LENGHT);
  
  /*
  (*client).println("HTTP/1.1 200 OK");
  (*client).println("Content-Type: text; charset=utf-8");
  (*client).println();  
  (*client).print(successStr + rowSeparator);
  (*client).print("Pin" + fieldSeparator + "Type" + fieldSeparator + "ConfiguredAs" + fieldSeparator + "status" + fieldSeparator + rowSeparator);
  
  for (int i = 0; i < SAMSUNG_COMMANDS; i++){
    (*client).print( (100+i) + fieldSeparator + commands[i].name + fieldSeparator + BUTTON + fieldSeparator+ "false" + fieldSeparator+ rowSeparator);
  }
  delay(1);
  (*client).stop();
  */
  
  
  
  
  return;
}

void changeChannel (EthernetClient *client, String *readString){  
  /*String tmp = andSeparator + relayStr + coupleSeparator;
  int trovato = (*readString).indexOf(tmp) + tmp.length();
  String command = (*readString).substring(trovato, trovato+3);
  int commandInt = command.toInt() - 100;
  Serial.println(commandInt);
  if (commandInt < 0 || commandInt >20){
    genericMessage(client, commandErr);
  } else {
    //irsend.sendRaw(commands[commandInt].raw,68,38);
    getPinConfiguration(client);
  }*/
  
  
  String tmp = andSeparator + valueStr + coupleSeparator;
  String command = (*readString).substring((*readString).lastIndexOf(tmp) + tmp.length(), (*readString).indexOf("HTTP/1.1")-1 );
  boolean isError = true;
  Serial.println("---" + command + "---");
  boolean found = false;
  for (int i = 0; i < SAMSUNG_COMMANDS && !found; i++){
    if (command == commands[i].name){
      getPinConfiguration(client);
      //irsend.sendRaw(commands[i].raw,68,38);
      delay(100);
      isError = false;
      found = true;
    }
  }
  
  if (isError){
    response[0] = "";
    response[0].concat(successStr + rowSeparator);
    response[0].concat(commandErr);
    sendGenericHttpResponse(client, response, 1);
  } 
  
  return;
}

void initCommands(){
  CommandStruct tmp = {"fake", S_pwr};
  int i;
  for (i = 0; i < SAMSUNG_COMMANDS; i++){
    commands[i] = tmp;
  }

  i = 0;
  commands[i].name = "S_pwr";
  commands[i].raw = S_pwr;
  i++;

  /*commands[i].name = "S_volumeUp";
  commands[i].raw = S_volumeUp;
  i++;
  
  commands[i].name = "S_volumeDown";
  commands[i].raw = S_volumeDown;
  i++;
  
  commands[i].name = "S_mute";
  commands[i].raw = S_mute;
  i++;
  
  commands[i].name = "S_channelUp";
  commands[i].raw = S_channelUp;
  i++;
  
  commands[i].name = "S_channelDown";
  commands[i].raw = S_channelDown;
  i++;
  */
  
  commands[i].name = "S_1";
  commands[i].raw = S_1;
  i++;
  
  /*
  commands[i].name = "S_2";
  commands[i].raw = S_2;
  i++;
  
  commands[i].name = "S_3";
  commands[i].raw = S_3;
  i++;
  
  commands[i].name = "S_4";
  commands[i].raw = S_4;
  i++;
  
  commands[i].name = "S_5";
  commands[i].raw = S_5;
  i++;
  
  commands[i].name = "S_6";
  commands[i].raw = S_6;
  i++;
  
  commands[i].name = "S_7";
  commands[i].raw = S_7; 
  i++;
  
  commands[i].name = "S_8";
  commands[i].raw = S_8;
  i++;
  
  commands[i].name = "S_9";
  commands[i].raw = S_9;
  i++;
  
  commands[i].name = "S_0";
  commands[i].raw = S_0;
  i++;
  
  commands[i].name = "S_guide";
  commands[i].raw = S_guide;
  i++;
  
  commands[i].name = "S_exit";
  commands[i].raw = S_exit;
  i++;
  
  commands[i].name = "S_mute";
  commands[i].raw = S_mute;
  */
  
  /*commands[1].name = "S_tv";
  commands[1].raw = S_tv;
  */

}




