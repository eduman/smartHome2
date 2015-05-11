#ifndef AppleRemoteSender_h
#define AppleRemoteSender_h

/*

Apple Remote Sender:
(c) Casey Callendrello 2008
Released under GPL v. 2

Notes: 
1) Apple's remotes use the NEC IR protocol, which is better
described here: http://www.sbprojects.com/knowledge/ir/nec.htm
It uses a 38 kHz carrier wave ( hence the OscWrite call) and uses a 
"pulse distance" encoding.  In other words, the LED is ON for the same
amount of time - it is the duration OFF that matters.

The protocol header is a 9ms on, followed by 4.5 ms off.  A '1' value is
.560 ms on, followed by 1.690 ms off. A '0' value is the same on pulse,
followed by .565 ms off.
The 'end' header is a .560 ms on pulse.

2) The total data transmitted is 32 bits. 

****** Remote Code *******
The first byte is the remote ID, 
a number between 0 and 255.

The second byte is the command
Known commands:
0x02: Menu
0x04: Play
0x07: Right
0x08: Left
0x0B: Up
0x0D: Down

The last two bytes are 0x87EE - which identifies this as an Apple device.


*/
#include <inttypes.h>
#include <avr/io.h>
#include "Arduino.h"

//suffix that all remotes use.
#define APPLE_ID 0x87EE

#define MENU 0x02
#define PLAY 0x04
#define RIGHT 0x07
#define LEFT 0x08
#define UP 0x0B
#define DOWN 0x0D


class AppleRemoteSender 
{
   public:
	AppleRemoteSender(int irpin);
	AppleRemoteSender(int irpin, byte remote_id);
	
	void set_remote_id(byte remote_id);
	
	void send(byte remote_id, byte key);
	void send(byte key);
	
	void menu(byte remote_id) ;
	void menu();
	
	void play(byte remote_id);
	void play();
	
	void right(byte remote_id);
	void right();
	
	void left(byte remote_id);
	void left();
	
	void up(byte remote_id);
	void up();
	
	void down(byte remote_id);
	void down();
private:
	long data;
	int _irpin;
	byte _remote_id;
	void oscWrite(int time);
};


#endif
