#!/usr/bin/python

from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler
from SocketServer import ThreadingMixIn
import threading
import json
import urlparse
from pprint import pprint
import os.path
import socket
import sys
import subprocess
from subprocess import Popen, PIPE
from shlex import split
from daemon import Daemon
import RPi.GPIO as io


#class IcmpPingTask(threading.Thread):
#       def get_default_gateway_linux():
#               """Read the default gateway directly from /proc."""
#               with open("/proc/net/route") as fh:
#                       for line in fh:
#                               fields = line.strip().split()
#                               if fields[1] != '00000000' or not int(fields[3], 16) & 2:
#                                       continue
#
#                               return socket.inet_ntoa(struct.pack("<L", int(fields[2], 16)))
#
#       def run(self):
#               while True:
#                       ping_str = "/bin/ping -i 5 -c 10 " # str (get_default_gateway_linux())
#                       subprocess.call(ping_str, shell=True)
#
#               return


class UDPDiscoverServerTask(threading.Thread):
   def run(self):
      str_max_size = 1024
      discover_str = "discover"
      on_str = "on"
      off_str = "off"

      io.setmode(io.BCM)
      power_pin = 23
      io.setup(power_pin, io.OUT)

      # Create a TCP/IP socket
      sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

      # Bind the socket to the port
      server_address = ('0.0.0.0', 8080)
      print >> sys.stderr, 'Starting UDP Server on %s port %s' % server_address
      sock.bind(server_address)

      while True:
         discover_send_str = ""
         data, address = sock.recvfrom(str_max_size)
         print >> sys.stderr, 'received %s bytes from %s' % (len(data), address)
         print >> sys.stderr, data

         if data == discover_str:
             f = open('/tmp/ifconfig','w+')
             output = str (subprocess.call("/sbin/ifconfig | grep \"inet addr\" | awk -F: '{print $2}' | awk '{print $1}'", shell=True, stdout=f))
             f.flush()
             f.close()
             f = open('/tmp/ifconfig','r')
             for line in f:
                if line.strip() != "127.0.0.1":
                    discover_send_str = discover_send_str + line.strip() + ":8080/discover|"
             f.close()
             print discover_send_str

             sent = sock.sendto(discover_send_str, address)
             print >>sys.stderr, 'sent %s bytes back to %s' % (sent, address)
         elif data == on_str:
             print("POWER ON")
             io.output(power_pin, True)
             sent = sock.sendto(on_str, address)
             print >>sys.stderr, 'sent %s bytes back to %s' % (sent, address)
         elif data == off_str:
             print("POWER OFF")
             io.output(power_pin, False)
             sent = sock.sendto(off_str, address);
             print >>sys.stderr, 'sent %s bytes back to %s' % (sent, address)
         else:
             sent = sock.sendto("error", address);
             print >>sys.stderr, 'sent %s bytes back to %s' % (sent, address)

      return

class PostHandler(BaseHTTPRequestHandler):
    def do_GET_handle_discover(self):
        if os.path.isfile("/etc/powerswitch/services.json"):
            file = open("/etc/powerswitch/services.json", "r")
        else:
            if os.path.isfile("services.json"):
                file = open("services.json", "r");
            else:
                file = open("services.json", "w+")
                file.write('{"services":[{"id":"1","name":"service1"}]}');

        self.send_response(200)
        self.end_headers()
        self.wfile.write(file.read())
        self.wfile.write('\n')
        return;

    def do_GET_handle_send_404(self):
        self.send_response(404)
        self.end_headers()
        self.wfile.write('Invalid GET request\n')
        return;

    def do_GET(self):
        parsed_path = urlparse.urlparse(self.path)
        parsed_path_split = parsed_path.path.split("/")

        pprint(parsed_path_split)

        if len(parsed_path_split) >= 2:
            if parsed_path_split[1] == "discover":
                self.do_GET_handle_discover()
            else:
                self.do_GET_handle_send_404()
        else:
            self.do_GET_handle_send_404()

        return;

    def do_POST(self):
        # Begin the response
        self.send_response(200)
        self.end_headers()
        self.wfile.write('Client: %s\n' % str(self.client_address))
        self.wfile.write('User-agent: %s\n' % str(self.headers['user-agent']))
        self.wfile.write('Path: %s\n' % self.path)

        post_data = self.rfile.read(int(self.headers.getheader('content-length')))
        self.wfile.write('Form data: %s\n' % str(post_data))

        post_data_json = json.loads(post_data)

        pprint(post_data_json)
        self.wfile.write('post_data_json[services][0][name]: %s\n' % str(post_data_json["services"][0]["name"]))

        return

class ThreadedHTTPServer(ThreadingMixIn, HTTPServer):
    """Handle requests in a separate thread."""


class MyDaemon(Daemon):
    def run(self):
        udpSocketServerTask = UDPDiscoverServerTask()
        udpSocketServerTask.start()

        server = ThreadedHTTPServer(('0.0.0.0', 8080), PostHandler)
        print 'Starting server, use <Ctrl-C> to stop'
        server.serve_forever()

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

#if __name__ == '__main__':
#    udpSocketServerTask = UDPDiscoverServerTask()
#    udpSocketServerTask.start()
#
#    server = ThreadedHTTPServer(('0.0.0.0', 8080), PostHandler)
#    print 'Starting server, use <Ctrl-C> to stop'
#    server.serve_forever()
