#!/bin/sh 
osascript <<END 
set volume output volume ((output volume of (get volume settings)) + 10)
END

