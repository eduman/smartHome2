#!/bin/sh 
osascript <<END 
tell application "System Events"
	set frontApp to name of first application process whose frontmost is true 
end tell
if frontApp is equal to "VLC" then
	tell application "VLC" 
		stop
	end tell	
else
	tell application "iTunes"
		stop
	end tell
end if
END

./agents/macosx/getConfiguration.sh
