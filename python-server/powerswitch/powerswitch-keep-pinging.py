#!/usr/bin/python

import sys
from daemon import Daemon
import subprocess
import socket, struct

class MyPingDaemon(Daemon):
   def get_default_gateway_linux():
      """Read the default gateway directly from /proc."""
      with open("/proc/net/route") as fh:
         for line in fh:
            fields = line.strip().split()
            if fields[1] != '00000000' or not int(fields[3], 16) & 2:
               continue

            return socket.inet_ntoa(struct.pack("<L", int(fields[2], 16)))

   def run(self):
      while True:
         ping_str = "/bin/ping -i 10 -c 10 " + str (get_default_gateway_linux())
         subprocess.call(ping_str, shell=True)


if __name__ == "__main__":
        daemon = MyPingDaemon('/tmp/keep-pinging.pid')
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