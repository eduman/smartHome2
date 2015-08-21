package it.eduman.smartHome.IoTDevice;

public class ActuationCommands {

	public enum CommonCommands{UnknownCommand, Measurements};
//	public enum DropBoxActuationCommands {Suspend_Periodic_Sync, Restart_Periodic_Sync};
	public enum ScannerActuationCommands {Scan};
	public enum ArduinoActuationCommands {getNetConfiguration, switchOn, switchOff, getActuatorStatus, getPinConfiguration};
	public enum ComputerActuationCommands {VolumeUp, VolumeDown, VolumeMute, Sleep, BrightnessUp, BrightnessDown, 
				PlayerPlayPause, PlayerStop, PlayerPrevious, PlayerNext};
	public enum RaspberryActuationCommands {Poweroff, Reboot};

//	public enum AndroidActuationCommands {};
}
