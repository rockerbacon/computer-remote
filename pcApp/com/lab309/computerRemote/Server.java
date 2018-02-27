package com.lab309.computerRemote;

import java.net.InetAddress;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import com.lab309.general.ByteArrayConverter;
import com.lab309.general.SizeConstants;

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
import com.lab309.security.Cipher;

import java.security.SecureRandom;

import java.io.IOException;
import java.net.SocketException;
import javax.crypto.IllegalBlockSizeException;

/*
 *	Class for managing connections and receiving commands (but not executing them).
 *	Server has an UDPServer connected to the breadcast port specified in Constants.broadcastPort, from where it receives visibility requests (Server.broadcastServer).
 *  Server has an UDPServer connected to a random port available at the moment of the object's instantiation from where it receives connection messages.
 *	Server has an UDPServer connected to a random port available at the moment of the object's instantiation from where it receives commands to be executed.
 *	Server uses RC4 for security of messages and SHA-256 for storing the password
 *  A validation byte is used to ensure that the decryption of messages worked correctly. If a validation byte is wrong the wrong public key or cipher was used to encrypt the message
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
	private byte validationByte;
	private Cipher cipher;
	
	private String name;
	private InetAddress ip;
	
	private UDPServer broadcastServer;
	private UDPServer connectionServer;
	private UDPServer commandsServer;
	
	private CommandsQueue commandQueue;
	private Object signal;
	
	private Steward steward;
	
	private Map<InetAddress, Connection> connections;
	
	/*CONSTRUCTORS*/
	public Server (byte validationByte, String name, byte[] password) throws IOException {
		this.validationByte = validationByte;
		if (validationByte != 0) {
			if (password == null) {
				Random rnd = new Random();
				password = new byte[Constants.passwordSize];
				rnd.nextBytes(password);
			}
			this.setPassword(password);
			System.out.println("Key: "+ByteArrayConverter.toStringRepresentation(this.cipher.getKey()));	//debug
		} else {
			this.cipher = null;
		}
 		
 		if (name == null) {
			this.name = InetAddress.getLocalHost().getHostName();
			//System.out.println(this.name);	//debug
		} else {
			this.name = name;
		}
		this.ip = NetInfo.thisMachineIpv4();

		this.broadcastServer = new UDPServer(Constants.broadcastPort, Constants.broadcastBufferSize, null);
		this.connectionServer = new UDPServer(Constants.connectionBufferSize, this.cipher);
		
		this.commandsServer = new UDPServer (Constants.commandBufferSize, this.cipher);
		this.commandQueue = new CommandsQueue (Constants.commandQueueSize);
		this.signal = new Object();
		
		this.steward = new Steward(this.commandQueue);
		
		this.broadcastAvailability();
		this.waitForConnections();
		//this.checkConnections();
		this.waitForCommands();
	}

	/*GETTERS*/
	public String getName() {
		return this.name;
	}
	
	public InetAddress getAddress() {
		return this.ip;
	}
	
	/*SETTERS*/
	public void setName (String name) {
		this.name = name;
	}
	
	public void setPassword (byte[] password) {
		this.cipher = new RC4Cipher(new SHA256Hasher().hash(password));
	}

	/*METHODS*/
	public void broadcastAvailability () { new Thread (new Runnable() { public void run() {
		try {
			UDPDatagram received;
			UDPClient client;
			UDPDatagram packet = new UDPDatagram(SizeConstants.sizeOfString(Server.this.name)+SizeConstants.sizeOfInt+SizeConstants.sizeOfByte);
			byte[] helloMsg = ByteArrayConverter.latinStringToArray(Constants.helloMessage, new byte[SizeConstants.sizeOfLatinString(Constants.helloMessage)], 0);
			
			packet.getBuffer().pushString(Server.this.name);
			packet.getBuffer().pushInt(Server.this.connectionServer.getPort());
			packet.getBuffer().pushByte(Server.this.validationByte);
			
			while (true) {
				//wait for hello message
				received = Server.this.broadcastServer.receiveExpected(helloMsg);
				
				//answer hello message
				client = new UDPClient (Constants.broadcastPort,received.getSender(), null);
				System.out.println("Received availability request from " + received.getSender().toString());	//debug
				try {
					client.send(packet);
				} catch (IllegalBlockSizeException e) {
					e.printStackTrace();
				}
				client.close();
			}
		} catch (SocketException e) {
			System.out.println (e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}}).start();
	}
	
	public void waitForConnections () { new Thread ( new Runnable () { public void run () {

		try {
			UDPDatagram received;
			UDPClient client;
			UDPDatagram packet = new UDPDatagram(SizeConstants.sizeOfLatinString(Constants.connectMessage)+SizeConstants.sizeOfInt);
			byte[] connectMessage = ByteArrayConverter.latinStringToArray(Constants.connectMessage, new byte[SizeConstants.sizeOfLatinString(Constants.connectMessage)], 0);
			byte[] fconnectMessage = ByteArrayConverter.latinStringToArray(Constants.finishConnectMessage, new byte[SizeConstants.sizeOfLatinString(Constants.finishConnectMessage)], 0);
			Connection connection;
			int answerPort;

			packet.getBuffer().pushLatinString(Constants.connectMessage);
			packet.getBuffer().pushInt(Server.this.commandsServer.getPort());
			while (true) {

				//wait for connection message
				received = Server.this.connectionServer.receiveExpected(connectMessage);
				//System.out.println("Received connection request from " + received.getSender().toString());	//debug
				
				answerPort = received.getBuffer().retrieveInt();
				
				//answer connection message
				client = new UDPClient (answerPort, received.getSender(), Server.this.cipher);
				
				try {
					client.send(packet);
				} catch (IllegalBlockSizeException e) {
					e.printStackTrace();
					return;
				}
				client.close();
				
				//wait for final handshake
				received = Server.this.broadcastServer.receiveExpectedOnTime(fconnectMessage, Constants.answerTimeLimit, Constants.wrongAnswerLimit);
				if (received == null) {
					continue;
				}
				
				connection = new Connection();
				connection.feedbackPort = received.getBuffer().retrieveInt();
				connection.name = received.getBuffer().retrieveString();
				
				//finish establishing connection
				Server.this.connections.put(received.getSender(), connection);
			}

		} catch (SocketException e) {
			System.out.println (e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}}).start();
	}
	
	/*
	private void checkConnections () { new Thread ( new Runnable () { public void run() {
		try {
			byte[] newKey = new byte[Constants.publicKeySize];
			byte[] checkMessage = ByteArrayConverter.stringToArray(Constants.connectionCheckMessage, new byte[SizeConstants.sizeOfString(Constants.connectionCheckMessage)], 0);
			SecureRandom rnd = new SecureRandom();
			UDPServer server = new UDPServer(SizeConstants.sizeOfString(Constants.connectionCheckMessage), Server.this.cipher);
			
			UDPDatagram packet = new UDPDatagram(SizeConstants.sizeOfString(Constants.connectionCheckMessage)+SizeConstants.sizeOfInt+newKey.length);
			UDPDatagram received;
			
			packet.getBuffer().pushString(Constants.connectionCheckMessage);
			packet.getBuffer().pushInt(server.getPort());
			packet.getBuffer().pushByteArray(newKey);
			
			while (true) {
				rnd.nextBytes(newKey);
				
				for (Map.Entry<InnetAddress, Connection> connection : Server.this.connections.entrySet()) {
					UDPClient client = new UDPClient (connection.getValue().feedbackPort, connection.getKey(), Server.this.cipher);
					client.send(packet);
					client.close();
					
					received = server.receiveExpectedOnTime(checkMessage, Constants.answerTimeLimit, Constants.wrongAnswerLimit);
					if (received == null) {
						Server.this.connections.remove(connection.getKey());
					}
				}
				
				Server.this.cipher.setKey(newKey);	
			}
		} catch (SocketException e) {
			System.out.println (e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}}).start();
	}
	*/
	
	private void waitForCommands () { new Thread ( new Runnable () { public void run () {
		try {
			UDPDatagram received;
			byte [] validationByte = new byte[1];
			validationByte[0] = Server.this.validationByte;

			while (true) {
				received = Server.this.commandsServer.receiveExpected(validationByte);
				if (Server.this.connections.containsKey(received.getSender())) {
					Server.this.commandQueue.push(received);
				}
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
		this.connectionServer.close();
		this.commandsServer.close();
	}

}

