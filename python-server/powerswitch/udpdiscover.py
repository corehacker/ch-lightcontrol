#!/usr/bin/python

import socket
import sys

if len(sys.argv) == 2:
    expected_text = sys.argv[1];
else:
    expected_text = "ON";

# Create a UDP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

server_address = ('localhost', 10000)
message = expected_text;

try:

    # Send data
    print >>sys.stderr, 'sending "%s"' % message
    sent = sock.sendto(message, server_address)

    # Receive response
    print >>sys.stderr, 'waiting to receive'
    data, server = sock.recvfrom(len (expected_text))
    
    if data == expected_text:
        print >>sys.stderr, 'received "%s"' % data
    else:
        print >>sys.stderr, 'Invalid message received: ' % data

finally:
    print >>sys.stderr, 'closing socket'
    sock.close()

