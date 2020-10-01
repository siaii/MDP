import socket
import time

SERVER_HOST = '192.168.7.7'  # The server's hostname or IP address
SERVER_PORT = 8090        # The port used by the server
SOCKET_BUFFER_SIZE = 512 

class Client_Algorithm:
	def __init__(self, host = SERVER_HOST, port = SERVER_PORT):
		self.server_host = SERVER_HOST
		self.server_port = SERVER_PORT

		self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
		self.socket.connect((self.server_host, self.server_port))

	def read(self):
		message = self.socket.recv(SOCKET_BUFFER_SIZE).strip()
		print("From RPI: ")
		print("\t" + repr(message))
		return repr(message)

	def write(self, message):
		print("To RPI: ")
		print("\t" + message)
		self.socket.sendall(message.encode('utf-8'))

client = Client_Algorithm()
a = 0
# while 1:
	# a = a + 1
	# client.write(str(a))
	# time.sleep(5)
client.write("1")
while 1:
	client.read()