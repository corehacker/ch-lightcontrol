#!/usr/bin/python

import time 
import RPi.GPIO as io 
io.setmode(io.BCM) 
 
power_pin = 23
 
io.setup(power_pin, io.OUT)
print("POWER OFF")
io.output(power_pin, False)
