#!/usr/bin/python


import sys
import threading
import paho.mqtt.client as mqtt
import os
import inspect
cmd_subfolder = os.path.realpath(os.path.abspath(os.path.join(os.path.split(inspect.getfile( inspect.currentframe() ))[0],"../src")))
if cmd_subfolder not in sys.path:
    sys.path.insert(0, cmd_subfolder)
import paho.mqtt.client as mqtt

class MyMQTTClass:
    def __init__(self, clientid, logger, notifier):
        self.logger = logger
        self.notifier = notifier
        self._mqttc = mqtt.Client(clientid, False)
        self._mqttc.on_message = self.mqtt_on_message
        self._mqttc.on_connect = self.mqtt_on_connect
        self._mqttc.on_publish = self.mqtt_on_publish
        self._mqttc.on_subscribe = self.mqtt_on_subscribe
        self._mqttc.on_unsubscribe = self.mqtt_on_unsubscribe
        self.__lock = threading.Lock()

    def mqtt_on_connect(self, mqttc, obj, flags, rc):
        self.logger.debug("Connected to message broker with result code: "+str(rc))

    def mqtt_on_message(self, mqttc, obj, msg):
        #self.logger.debug("Message received: " + msg.topic+" "+str(msg.qos)+" "+str(msg.payload))
        self.notifier.notifyJsonEvent(msg.topic, msg.payload)

    def mqtt_on_publish(self, mqttc, obj, mid):
        self.logger.debug("mid: "+str(mid))

    def mqtt_on_subscribe(self, mqttc, obj, mid, granted_qos):
        self.logger.debug("Subscribed: "+str(mid)+" "+str(granted_qos))

    def mqtt_on_unsubscribe(self, mqttc, obj, mid):
        self.logger.debug("Unsubscribed: "+str(mid))

    def mqtt_on_log(self, mqttc, obj, level, string):
        self.logger.debug(string)



    def loop(self):
        try:
            self._mqttc.loop_forever()
        except Exception, e:
            self.logger.error("Erron on loop() %s", e)


    def setUserAuthentication(self, username, password=None):
        self._mqttc.username_pw_set(username, password)


    def disconnect(self):
        try:
            self.logger.info("Disconnecting from message broker")
            self._mqttc.disconnect()
            self._mqttc.loop_stop(force=False)
            self.timer.cancel()
        except Exception, e:
            self.logger.error("Erron on mqttDisconnect() %s", e)


    def connect(self, uri="localhost", port=1883, userdata=60):
        try:
            self._mqttc.connect(str(uri), str(port), userdata)
            self._mqttc.loop_start()
            #t1 = threading.Thread(target=self.loop)
            #t1.start()
            #self.timer = threading.Timer(300.0, self.mqttReconnect)
            #self.timer.start()
        except Exception, e:
            self.logger.error("Erron on registerMQTT() %s", e)

    def mqttReconnect(self):
        try:
            self._mqttc.reconnect()
            #self.timer = threading.Timer(300.0, self.mqttReconnect)
            #self.timer.start()
        except Exception, e:
            self.logger.error("Erron on mqttReconnect() %s", e) 

    def subscribeEvent(self, fullString, topic):
        subscribedEvents = []
        if fullString:
            tokens =  ''.join(str(fullString).split()).split(';')
            for tok in tokens:
                if tok:
                    event = topic + "/" + tok.lower() + "/#"
                    try: 
                        self._mqttc.subscribe(event)
                        subscribedEvents.append(event)
                        self.logger.info("Subscribed for the event: %s " % event)
                    except Exception, e:
                        self.logger.error("Error on subscribeEvent() %s", e)
        else:
            event = topic + "/#"
            self._mqttc.subscribe(event)
            subscribedEvents.append(event)
            self.logger.info("Subscribed for the event: %s " % event)

        return subscribedEvents

    def unsubscribeEvent(self, event):
        try:
            self._mqttc.unsubscribe(event)
            self.logger.info("Unsubscribed for the event: %s " % event)
        except Exception, e:
            self.logger.error("Error on unsubscribeEvent() %s", e)


    def publish(self, eventTopic, payload, qos=2):
        #self._mqttc.publish(eventTopic, payload, qos)
        self.syncPublish(eventTopic, payload, qos)

    def syncPublish(self, eventTopic, payload, qos=2):
        self.__lock.acquire()
        self._mqttc.publish(eventTopic, payload, qos)
        self.__lock.release()
        self.logger.info('Publishing topic: "%s" with msg: %s ' % (eventTopic, payload))

