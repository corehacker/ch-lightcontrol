#!/usr/bin/python

import socket
import sys
from daemon import Daemon
import time
import RPi.GPIO as io

class MyDaemon(Daemon):
	def run(self):
		io.setmode(io.BCM)
		power_pin = 23
		io.setup(power_pin, io.OUT)


		# Create a TCP/IP socket
		sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

		# Bind the socket to the port
		server_address = ('0.0.0.0', 10000)
		print >>sys.stderr, 'starting up on %s port %s' % server_address
		sock.bind(server_address)

		while True:
			print >>sys.stderr, '\nwaiting to receive message'
			data, address = sock.recvfrom(2);

			print >>sys.stderr, 'received %s bytes from %s' % (2, address)
			print >>sys.stderr, data

			if data:
				if data == "ON":
					print("POWER ON")
					io.output(power_pin, True)
					sent = sock.sendto("ON", address)
					print >>sys.stderr, 'sent %s bytes back to %s' % (sent, address)
				else:
					print("POWER OFF")
					io.output(power_pin, False)
					sent = sock.sendto("OF", address);
					print >>sys.stderr, 'sent %s bytes back to %s' % (sent, address)


if __name__ == "__main__":
        daemon = MyDaemon('/tmp/powerswitch.pid')
        if len(sys.argv) == 2:
                if 'start' == sys.argv[1]:
                        daemon.start()
                elif 'stop' == sys.argv[1]:
                        daemon.stop()
                elif 'restart' == sys.argv[1]:
                        daemon.restart()
                else:
                        print "Unknown command"
                        sys.exit(2)
                sys.exit(0)
        else:
                print "usage: %s start|stop|restart" % sys.argv[0]
                sys.exit(2)
