package com.lab309.computerRemote;

import com.lab309.network.NetInfo;
import com.lab309.network.UDPClient;
import com.lab309.network.UDPServer;
import com.lab309.network.UDPDatagram;

import com.lab309.security.Cipher;
import com.lab309.security.RC4Cipher;
import com.lab309.security.SHA256Hasher;

import com.lab309.general.SizeConstants;
import com.lab309.general.ByteArrayConverter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.crypto.IllegalBlockSizeException;

/**
 * Created by Vitor Andrade dos Santos on 4/13/17.
 */

public class Client implements Serializable {

	/*ATTRIBUTES*/
	private String name;
	private InetAddress ip;
	private LinkedList<InetAddress> broadcasters;
	private ArrayList<ServerModel> availableServers;
	private ArrayList<ServerModel> connectedServers;
	private transient Object availableServersLock = new Object();
	private transient Object connectedServersLock = new Object();

	/*CONSTRUCTORS*/
	public Client (String name) throws IOException {
		this.name = name;
		this.ip = NetInfo.thisMachineIpv4();
		this.broadcasters = NetInfo.broadcastIp();
		if (this.broadcasters.size() == 0 || this.ip == null) {
			throw new IOException("Could not connect to network");
		}
		this.availableServers = new ArrayList<ServerModel>();
		this.connectedServers = new ArrayList<ServerModel>(3);
	}

	/*GETTERS*/
	public String getName () {
		return this.name;
	}

	public int getAvailableServersCount () {
		synchronized (this.availableServersLock) {
			return this.availableServers.size();
		}
	}

	public ServerModel getAvailableServer (int index) {
		synchronized (this.availableServersLock) {
			return this.availableServers.get(index);
		}
	}

	public int getConnectedServersCount () {
		synchronized (this.connectedServersLock) {
			return this.connectedServers.size();
		}
	}

	public ServerModel getConnectedServer (int index) {
		synchronized (this.connectedServersLock) {
			return this.connectedServers.get(index);
		}
	}

	/*METHODS*/
	public void searchServers () throws IOException {
		UDPClient client;
		UDPServer server = new UDPServer (Constants.broadcastPort, Constants.maxName+SizeConstants.sizeOfInt+SizeConstants.sizeOfByte, null);
		UDPDatagram packet = new UDPDatagram(SizeConstants.sizeOfString(Constants.helloMessage));
		
		packet.getBuffer().pushString(Constants.helloMessage);
		
		//broadcast hello message
		for (InetAddress addr : this.broadcasters) {
			client = new UDPClient(Constants.broadcastPort, addr, null);
			try {
				client.send(packet);
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			}

			client.close();
		}
		
		//populate list of available servers
		while ( (packet = server.receiveOnTime(Constants.answerTimeLimit, Constants.wrongAnswerLimit)) != null) {
			if (this.ip.equals(packet.getSender())) {
				continue;
			}
			String name = packet.getBuffer().retrieveString();
			int connectionPort = packet.getBuffer().retrieveInt();
			byte validationByte = packet.getBuffer().retrieveByte();
			ServerModel availableServer = new ServerModel(packet.getSender(), name, connectionPort, validationByte);
			
			this.availableServers.add(availableServer);
		}
		
	}

	/*
	 *	Returns:
	 * 		UDPServer.STATUS_SUCCESSFUL if the connection was established successfuly
	 * 		UDPServer.STATUS_PACKET_NOT_EXPECTED if something went wrong with the encryption
	 *		UDPServer.STATUS_TIMEOUT if server did not respond (possibly invalid password)
	 *
	 */
	public int connectToServer (ServerModel connection, byte[] password) throws IOException {
	
		Cipher cipher;
		if (password != null) {
			cipher = new RC4Cipher(new SHA256Hasher().hash(password));
		} else {
			cipher = null;
		}

		UDPClient client = new UDPClient(connection.getConnectionPort(), connection.getAddress(), cipher);
		UDPServer server = new UDPServer(SizeConstants.sizeOfString(Constants.connectMessage)+SizeConstants.sizeOfInt, cipher);
		UDPDatagram packet = new UDPDatagram (SizeConstants.sizeOfString(Constants.finishConnectMessage)+Constants.maxName+SizeConstants.sizeOfInt);
		byte[] connectMessage = ByteArrayConverter.stringToArray(Constants.connectMessage, new byte[SizeConstants.sizeOfString(Constants.connectMessage)], 0);
		UDPDatagram received;
		int commandsPort;
		
		packet.getBuffer().pushString(Constants.connectMessage);
		packet.getBuffer().pushInt(server.getPort());
		try {
			client.send(packet);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return UDPServer.STATUS_PACKET_NOT_EXPECTED;
		}
		packet.getBuffer().rewind();

		received = server.receiveExpectedOnTime(connectMessage, Constants.answerTimeLimit, Constants.wrongAnswerLimit);
		if (received == null) {
			return UDPServer.STATUS_TIMEOUT;
		}
		
		commandsPort = received.getBuffer().retrieveInt();
		connection.confirmConnection(cipher.getKey(), commandsPort);
		
		packet.getBuffer().pushString(Constants.finishConnectMessage);
		packet.getBuffer().pushInt(connection.getFeedbackServer().getPort());
		packet.getBuffer().pushString(this.name);

		try {
			client.send(packet);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return UDPServer.STATUS_PACKET_NOT_EXPECTED;
		}
		
		this.connectedServers.add(connection);
		
		return UDPServer.STATUS_SUCCESSFUL;

	}

	/*
	public void checkConnections () throws IOException {
		synchronized (this.connectedServersLock) {

			this.clearAvailableServers();
			this.requestIdentities();

			for (int i = 0; i < this.connectedServers.size(); i++) {

				ServerModel server = this.connectedServers.remove(i);

				try {
					if (this.connectToServer(server, server.getPassword()) != UDPServer.STATUS_SUCCESSFUL) {
						int j;
						ServerModel search;

						j = 0;
						do {
							search = this.availableServers.get(j);
							j++;
						} while (j < this.availableServers.size() && !search.getName().equals(server.getName()));

						if (search.getName().equals(server.getName())) {
							this.connectToServer(search, server.getPassword());
						}
					}
				} catch (IOException e) {
					this.connectedServers.remove(i);
				}

			}

		}

	}
	*/

	/*
	public static void executeLine (final ServerModel server, final String line) {
		new Thread (new Runnable () {
			@Override
			public void run () {
				try {
					UDPDatagram command;

					command = Client.prepareCommandDatagram(server, SizeConstants.sizeOfString(line));

					command.pushInt(Constants.commandExecuteLine);
					command.pushString(line);

					server.send(command);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void keyboardPress (final ServerModel server, final int keycode) {
		new Thread ( new Runnable() {
			@Override
			public void run() {
				try {
					UDPDatagram command;

					command = Client.prepareCommandDatagram(server, SizeConstants.sizeOfInt);

					command.pushInt(Constants.commandKeyboardPress);
					command.pushInt(keycode);

					server.send(command);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static void keyboardRelease (final ServerModel server, final int keycode) {
		new Thread ( new Runnable() {
			@Override
			public void run() {
				try {
					UDPDatagram command;

					command = Client.prepareCommandDatagram(server, SizeConstants.sizeOfInt);

					command.pushInt(Constants.commandKeyboardRelease);
					command.pushInt(keycode);

					server.send(command);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	//sound level deve ser um valor em porcentagem entre 0 e 1
	public static void setSound (final ServerModel server, final float soundLevel) {
		new Thread ( new Runnable() {
			@Override
			public void run() {
				try {
					UDPDatagram command;

					command = Client.prepareCommandDatagram(server, SizeConstants.sizeOfInt);

					command.pushInt(Constants.commandSetSoundLevel);
					command.pushFloat(soundLevel);

					server.send(command);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	*/

	public void clearAvailableServers () {
		synchronized (this.availableServersLock) {
			this.availableServers.clear();
		}
	}

	public void clearConnections () {
		synchronized (this.connectedServersLock) {
			for (ServerModel server : this.connectedServers) {
				server.disconnect();
			}
			this.connectedServers.clear();
		}
	}

	public void disconnect (int index) {
		synchronized (this.connectedServersLock) {
			this.connectedServers.get(index).disconnect();
			this.connectedServers.remove(index);
		}
	}

	private void readObject (ObjectInputStream input) throws IOException, ClassNotFoundException {
		input.defaultReadObject();
		this.availableServersLock = new Object();
		this.connectedServersLock = new Object();
	}
}
