#!/usr/bin/env python

import os
import socket
import sys
import time

# This file contains a client for executing tests provided by the
# dispatch server
# @author Troy Madsen

def main():

    # Wait duration in seconds
    WAIT_DURATION = 300

    # Connect to the server for file dispatch
    hostname_file = 'DispatcherHostname.txt'
    try:
        with open(hostname_file, 'r') as hostname:
            HOST = hostname.readline()
            PORT = int(hostname.readline())
    except:
        print('No hostname found')
        sys.exit()

    # If connection fails, terminate this worker
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        server.connect((HOST, PORT))
    except:
        server.close()
        sys.exit()

    # Request test file from dispatch server and execute it
    try:
        test_file = 'next'
        while True:

            # Wait for next test file to arrive
            recvd = ''
            while (len(recvd) == 0):
                server.send(test_file)
                recvd = server.recv(1024)
            test_file = recvd

            # Terminate worker if given quit command
            if (test_file == 'quit'):
                break

            # Wait for a period before asking for another test to run
            elif (test_file == 'wait'):
                print('Waiting...')
                time.sleep(10)

            # Execute the test
            elif (os.path.isfile(test_file)):
                print('Running test: ' + test_file)
#                time.sleep(5)
                os.system(test_file)
#                os.system('mv tests/' + test_file + ' completed/')

    except KeyboardInterrupt as i:
        print
    finally:
        print('Test worker shutting down')
        server.close()

# Determine if this is the main program running
if __name__ == '__main__':
    main()
