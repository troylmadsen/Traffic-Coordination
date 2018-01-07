#!/usr/bin/env python

import os
import re
import socket
import threading
from SocketServer import ThreadingMixIn

# This file contains a multithreaded python server for handling the dispatch of
# test files to connected clients
# @author Troy Madsen

# Class definition of a ClientThread that handles clients connected to the server
class ClientThread(threading.Thread):

    # Initializes ClientThread
    def __init__(self, conn, parent, ip, port):
        threading.Thread.__init__(self)
        self.conn = conn
        self.parent = parent
        self.clientIP = ip
        self.clientPort = port
        print('[+] New server socket thread started for ' + self.clientIP + ':' + str(self.clientPort))


    # Execution of the ClientThread
    def run(self):
        try:
            while True:
                query = self.conn.recv(1024)
                print('Server recieved query: ' + query)
                if query == 'quit':
                    break
                self.conn.send(self.parent.nextTest())
        except Exception as e:
            print(e)
        finally:
            print('[-] Server socket thread for ' + self.clientIP + ':' + str(self.clientPort) + ' shutting down')
            self.conn.close()



# Class definition of a DispatchServer that dispatchs file to clients for execution
class DispatchServer:

    # This function is responsible for locking nextTest
    def synchronized(func):
        func.__lock__ = threading.Lock()

        def synced_func(*args, **kws):
            with func.__lock__:
                return func(*args, **kws)

        return synced_func


    # Thread-safe function to provide the next test file for execution
    @synchronized
    def nextTest(self):
        test_file = 'wait'
        if (len(self.test_files) > 0):
            test_file = self.test_files.pop()
    
        return test_file


    # Execution of the DispatchServer
    def run(self):

        # Compile a list of test files to execute based on the provided regex expression
        TEST_REGEX = '.*Run.*\.py'
        FILE_DIR = os.getcwd() + '/tests/'
        all_files = os.listdir(FILE_DIR)
        self.test_files = filter(re.compile(TEST_REGEX).match, all_files)
        self.test_files = list(map(lambda f: FILE_DIR + f, self.test_files))

        # Multithreaded Python server running over a TCP connection
        HOST = socket.gethostbyname(socket.gethostname())
        PORT = 8771

        # Write the hostname to a file for clients to read
        hostname_file = 'DispatcherHostname.txt'
        with open(hostname_file, 'w') as hostname:
            hostname.write(HOST + '\n' + str(PORT))

        # Create server
        server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1) 
        server.bind((HOST, PORT)) 
        threads = []

        # Wait for clients to connect
        print('Server awaiting connections')
        print('Press ^C to exit')
        try:
            #FIXME Keep a running tally of the number of clients so that each may be shut down when tests exhausted
            while True:
                server.listen(4)
                (conn, (ip, port)) = server.accept()
                newthread = ClientThread(conn, self, ip, port)
                newthread.start()
                threads.append(newthread)
        except Exception as e:
            print(e)
        except KeyboardInterrupt as i:
            print
        finally:
            print('!!! Dispatch server shutting down !!!')
            for t in threads:
                t.join()



# Entry point of the program
def main():
    server = DispatchServer()
    server.run()


# Determine if this is the main program running
if __name__ == '__main__':
    main()
