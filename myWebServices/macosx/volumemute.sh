#!/bin/sh 
osascript <<END 
if output muted of (get volume settings) then 
	set volume output muted false
else 
	set volume output muted true
end if
END

