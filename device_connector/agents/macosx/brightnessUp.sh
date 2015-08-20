#!/bin/sh 
osascript <<END 
tell application "System Events" to repeat 1 times
	key code 113
end repeat
END

./agents/macosx/getConfiguration.sh
