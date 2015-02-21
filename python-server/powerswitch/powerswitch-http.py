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

class UDPDiscoverServerTask(threading.Thread):
    def run(self):
		# Create a TCP/IP socket
		sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

		# Bind the socket to the port
		server_address = ('0.0.0.0', 8080)
		print >>sys.stderr, 'Starting UDP Server on %s port %s' % server_address
		sock.bind(server_address)

		while True:
			data, address = sock.recvfrom(2);

			print >>sys.stderr, 'received %s bytes from %s' % (2, address)
			print >>sys.stderr, data

class PostHandler(BaseHTTPRequestHandler):
    def do_GET_handle_discover(self):
        if os.path.isfile("/etc/powerswitch/services.json"):
            file = open("/etc/powerswitch/services.json", "r")
        else:
            if os.path.isfile("services.json"):
                file = open("services.json", "r");
            else:
                file = open("/etc/powerswitch/services.json", "rw")
                file.write('{"services":[{"id":"1","name":"service1"}]}');

        self.send_response(200)
        self.end_headers()
        self.wfile.write(file.read())
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

if __name__ == '__main__':
    udpSocketServerTask = UDPDiscoverServerTask()
    udpSocketServerTask.start()

    server = ThreadedHTTPServer(('0.0.0.0', 8080), PostHandler)
    print 'Starting server, use <Ctrl-C> to stop'
    server.serve_forever()
