package com.lab309.computerRemote;

import com.lab309.network.MacAddress;
import com.lab309.network.NetInfo;
import com.lab309.network.UDPClient;
import com.lab309.network.UDPServer;
import com.lab309.network.UDPDatagram;

import com.lab309.general.SizeConstants;

import java.io.IOException;
import java.util.LinkedList;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by Vitor Andrade dos Santos on 4/13/17.
 */

public class Client {

	/*ATTRIBUTES*/
	private static String name;
	private InetAddress ip;
	private LinkedList<InetAddress> broadcasters;
	private ArrayList<ServerModel> availableServers;
	private ArrayList<ServerModel> connectedServers;
	private transient final Object availableServersLock = new Object();
	private transient final Object connectedServersLock = new Object();

	/*CONSTRUCTORS*/
	public Client (String name) throws IOException {
		Client.name = name;
		this.ip = NetInfo.thisMachineIpv4();
		this.broadcasters = NetInfo.broadcastIp();
		if (this.broadcasters.size() == 0 || this.ip == null) {
			throw new IOException("Could not connect to network");
		}
		this.availableServers = new ArrayList<ServerModel>();
		this.connectedServers = new ArrayList<ServerModel>(5);
	}

	/*GETTERS*/
	public String getName () {
		return Client.name;
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
		UDPServer server = new UDPServer (Constants.broadcastPort, Constants.maxName+SizeConstants.sizeOfInt+2*SizeConstants.sizeOfBoolean+Constants.publicKeySize, null);
		UDPDatagram packet = new UDPDatagram(SizeConstants.sizeOfString(Constants.helloMessage));
		ServerModel availableServer;
		
		packet.getBuffer().pushString(Constants.helloMessage);
		
		//broadcast hello message
		for (InetAddress addr : this.broadcasters) {
			client = new UDPClient(Constants.broadcastPort, addr, null);
			client.send(packet);
			client.close();
		}
		
		//populate list of available servers
		while ( (packet = server.receiveOnTime(Constants.answerTimeLimit, Constants.wrongAnswerLimit)) != null) {
			String name = packet.getBuffer().retrieveString();
			int connectionPort = packet.getBuffer().retrieveInt();
			boolean passProtected = packet.getBuffer().retrieveBoolean();
			boolean encrypted = packet.getBuffer().retrieveBoolean();
			byte[] publicKey = null;
			if (encrypted) publicKey = packet.getBuffer().retrieveByteArray(Constants.publicKeySize, new byte[Constants.publicKeySize], 0);
			
			ServerModel availableServer = new ServerModel(packet.getSender(), name, connectionPort, passProtected, publicKey);
			
			this.availableServers.add(availableServer);
		}
		
	}

	/*
	 *	Retorna:
	 * 		UDPServer.STATUS_SUCCESSFUL se conexao foi estabelecida
	 * 		UDPServer.STATUS_PACKET_NOT_EXPECTED se senha eh invalida
	 *		UDPServer.STATUS_TIMEOUT se servidor nao respondeu
	 *
	 */
	public static int connectToServer (ServerModel server, String password) throws IOException {

		RC4Cipher cipher = server.getCipher();
		UDPClient client = new UDPClient(server.getConnectionPort, server.getAddress, cipher);
		UDPServer server = new UDPServer(SizeConstants.sizeOfInt, cipher);
		UDPDatagram request = new UDPDatagram(SizeConstants.sizeOfString(Constants.finishConnectMessage)+SizeConstants.sizeOfInt+Constants.maxName);
		UDPDatagram answer;
		byte[] passHash = new SHA256Hasher().hash( ByteArrayConverter.stringToArray(password, new byte[SizeConstants.sizeOfString(password)], 0) );
		int serverPort;

		//send connection request
		request.getBuffer().pushString(Constants.connectMessage);
		request.getBuffer().pushInt(server.getPort());
		request.getBuffer().pushByteArray(passHash);
		client.send(request);

		//receive connection port
		answer = server.receiveOnTime(Constants.answerTimeLimit, Constants.wrongAnswerLimit);
		server.close();
		if (answer == null) {
			client.close();
			return UDPServer.STATUS_TIMEOUT;
		}

		serverPort = answer.getBuffer().retrieveInt();
		if (serverPort == -1) {
			client.close();
			return UDPServer.STATUS_PACKET_NOT_EXPECTED;
		}
		
		//finish connection
		server.confirmConnection(serverPort, password);
		
		request.getBuffer().rewind();
		request.getBuffer().pushString(Constants.finishConnectMessage);
		request.getBuffer().pushInt(server.getFeedbackServer().getPort());
		request.getBuffer().pushString(this.name);

		/*
		synchronized (this.availableServersLock) {
			this.availableServers.remove(index);
		}
		*/
		/*
		synchronized (this.connectedServersLock) {
			this.connectedServers.add(server);
		}
		*/

		client.close();
		return UDPServer.STATUS_SUCCESSFUL;

	}

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

	private static UDPDatagram prepareCommandDatagram (ServerModel server, int commandDataSize) {
		UDPDatagram command = new UDPDatagram (MacAddress.SIZE + SizeConstants.sizeOfString(Client.name) + SizeConstants.sizeOfInt + commandDataSize);

		command.pushByteArray(server.getMacAddress().getAddress(), 0, server.getMacAddress().getAddress().length);
		command.pushString(Client.name);

		return command;
	}

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
}
