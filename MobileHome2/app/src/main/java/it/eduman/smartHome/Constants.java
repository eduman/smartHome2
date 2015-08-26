package it.eduman.smartHome;


public class Constants {
    public enum DeviceType {arduino, plugwise, scanner, raspberry, vlc};
    public enum ProtocolType {GET, PUT, STREAM};
    public enum ProtocoloWS {vlc, rest};
    public enum ActuatorAndSensorProperties{Switch, Iswitch, Sensor, Input, Button};
}
