package com.lab309.computerRemote;

import java.net.InetAddress;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;

import java.awt.Robot;

import com.lab309.network.UDPClient;
import com.lab309.network.UDPServer;
import com.lab309.network.UDPDatagram;
import com.lab309.network.TCPServer;
import com.lab309.network.MacAddress;
import com.lab309.network.NetInfo;

import com.lab309.steward.CommandsQueue;
import com.lab309.steward.Steward;

import com.lab309.security.SHA256Hasher;
import com.lab309.security.RC4Cipher;

import com.lab309.general.SizeConstants;

import java.security.SecureRandom;

import java.io.IOException;
import java.net.SocketException;

/*
 *	Class for managing connections and receiving commands (but not executing them).
 *	Server has an UDPServer connected to the breadcast port specified in Constants.broadcastPort, from where it receives connection requests (Server.broadcastServer).
 *	Server has an UDPServer connected to a random port available at the moment of the object's instantiation from where it receives commands to be executed.
 *	Server uses RC4 for security of messages and SHA-256 for storing the password
 *	If any error occurs
 *
 *	The connection protocol goes as follows:
 *		1-Client broadcasts a packet through the broadcast port, having: (Constants.helloMessage)
 *
 *		2-Server sends a packet to the client's IP through the broadcast port, having:
 *			(String serverName, boolean isPasswordProtected, boolean isEncrypted, byte[] publicKey)
 *			The publicKey will only be present in case isEncrypted = true
 *
 *		3-Client sends a packet to the server's IP through the broadcast port, having: (Constants.connectMessage, byte[] password)
 *			Password needs to be present only if the server has a password
 *
 *		4-Server sends an encrypted packet to the client's IP through the broadcast port, having: (int commandsPort)
 *			If the password was not accepted commandsPort will be -1
 *
 *		5-Client sends an encrypted packet to the server's IP through the broadcast port, having: (Constants.connectMessage, int feedbackPort, String clientName), after which the connection is fully established
 *
 *		The server will send an encrypted package to the client't IP through the feedback port every Constants.connectionCheckInterval minutes, having:
 *			(Constants.connectionCheckMessage, int answerPort, byte[] publicKey)
 *			After that the client must answer with a package to the server's IP through the answer port having (Constants.connectionCheckMessage)
 *			If the client does not answer within the default time limit the connection will be terminated
 *	
 *
 *
 *	The transmission of commands follows the protocol:
 *		3-Client sends packet throught the command port to the server's IP, having: (byte commandId, ... args)
 *			All the contents, including the client name must be encrypted using RC4 if the server has encryption enabled
 *			For the arguments of each type of command see class Steward
 *
 *
 */
public class Server {

	/*
	 * Class for modeling a connection
	 */
	private static class Connection {
		/*ATRIBUTES*/
		public int feedbackPort;	//connection used for transmitting errors only
		public String name;
	}

	/*ATTRIBUTES*/
	private boolean encrypted;
	
	private Steward steward;
	
	private byte[] password;
	private String name;
	
	private UDPServer broadcastServer;
	private UDPServer commandsServer;
	
	private CommandsQueue commandQueue;
	
	private Map<InetAddress, Connection> connections;
	
	/*CONSTRUCTORS*/
	public Server (boolean encrypted) throws IOException {
		this.encrypted = encrypted;
		
 		this.steward = new Steward();
 		
		this.password = null;
		this.name = InetAddress.getLocalHost().getHostName();
		//System.out.println(this.name);	//debug

		this.commandsServer = new UDPServer (Constants.commandBufferSize);
		this.commandQueue = new ConcurrentStaticQueue<UDPDatagram> (Constants.commandQueueSize);
		this.signal = new Object();
		
		this.ciphers = new RC4Cipher[Constants.numberOfPublicKeys];
		for (int i = 0; i < this.ciphers.length; i++) {
			this.ciphers[i] = new RC4Cipher(Constants.publicKeySize);
		}	
		
		this.waitForConnections();
		this.waitForCommands();
	}

	/*GETTERS*/
	public String getName() {
		return name;
	}
	
	/*SETTERS*/
	public void setName (String name) {
		this.name = name;
	}
	
	public void setPassword (String password) {
		if (password == null) {
			this.password = null;
		} else {
			SHA256Hasher hasher = new SHA256Hasher();
			this.password = hasher.hash(password.getBytes());
		}
	}

	/*METHODS*/
	public void waitForConnections () { new Thread ( new Runnable () { public void run () {

		try {
			Server.this.broadcastServer = new UDPServer(Constants.broadcastPort, Constants.broadcastBufferSize);
			UDPDatagram received;
			UDPClient client;
			UDPDatagram packet = new UDPDatagram(Constants.maxName+2*SizeConstants.sizeOfBoolean+Constants.publicKeySize);
			byte[] password = new byte[32];
			byte[] connectMessage = ByteArrayConverter.stringToArray(Constants.connectMessage, new byte[SizeConstants.sizeOfString(Constants.connectMessage)], 0);
			int port;
			Connection connection;

			while (true) {

				//step 1
				received = Server.this.broadcastServer.receiveExpected(Constants.helloMessage);
				connection = new Connection();

				//step 2
				client = new UDPClient (received.getSender(), Constants.broadcastPort);
				
				packet.getBuffer().pushString(Server.this.name);
				packet.getBuffer().pushBoolean(Server.this.password != null);
				packet.getBuffer().pushBoolean(Server.this.encrypted);
				
				client.send(packet);
				packet.getBuffer().rewind();
				
				//step 3
				received = Server.this.broadcastServer.receiveExpectedOnTime(connectMessage, Constants.answerTimeLimit, Constants.wrongAnswerLimit);
				if (received == null) {
					client.close();
					continue;
				}
				
				if (Server.this.password != null) {
					received.getBuffer().retrieveByteArray(password.length, password, 0);
				
					if (Array.equals(password, Server.this.password)) {
						port = Server.this.commandsPort.getPort();
					} else {
						port = -1;
					}
				}
				
				//step 4
				packet.getBuffer().pushInt(port);
				client.send(packet);
				
				//step 5
				received = Server.this.broadcastServer.receiveExpectedOnTime(connectMessage, Constants.answerTimeLimit, Constants.wrongAnswerLimit);
				if (received == null) {
					client.close();
					continue;
				}
				
				connection.feedbackPort = received.getBuffer().retrieveInt();
				connection.name = received.getBuffer().retrieveString();
				
				//finish establishing connection
				Server.this.connections.put(received.getSender(), connection);
				client.close();
			}

		} catch (SocketException e) {
			System.out.println (e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}}).start();
	}
	
	private void waitForCommands () { new Thread ( new Runnable () { public void run () {
		try {
			UDPDatagram received;

			while (true) {
				received = Server.this.commandsServer.receive();
				Server.this.commandQueue.push(received);
			}
		} catch (SocketException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}}).start();
	}

	public void close () {
		this.broadcastServer.close();
		this.commandsServer.close();
	}

}

