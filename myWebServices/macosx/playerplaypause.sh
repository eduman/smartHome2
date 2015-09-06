#!/bin/sh 
osascript <<END 
tell application "System Events"
	set frontApp to name of first application process whose frontmost is true 
end tell
if frontApp is equal to "VLC" then
	tell application "VLC" 
		play
	end tell	
else
	tell application "iTunes"
		playpause
	end tell
end if
END

