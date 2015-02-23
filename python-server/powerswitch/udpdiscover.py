#!/usr/bin/python

import socket
import sys

if len(sys.argv) >= 2:
    server_hostname = str(sys.argv[1])
else:
    server_hostname = "localhost"

if len(sys.argv) >= 3:
    server_port = int(sys.argv[2])
else:
    server_port = 8080

if len(sys.argv) >= 4:
    expected_text = sys.argv[3];
else:
    expected_text = "ON";

# Create a UDP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

server_address = (server_hostname, server_port)
message = expected_text;

try:

    # Send data
    print >>sys.stderr, 'sending "%s"' % message
    sent = sock.sendto(message, server_address)

    # Receive response
    print >>sys.stderr, 'waiting to receive'
    data, server = sock.recvfrom(1024)

    print >>sys.stderr, 'received "%s"' % data

finally:
    print >>sys.stderr, 'closing socket'
    sock.close()

