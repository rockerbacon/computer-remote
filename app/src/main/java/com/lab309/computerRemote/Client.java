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
	private transient ArrayList<ServerModel> availableServers;
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
	public void requestIdentities () throws IOException {
		UDPDatagram received = null;
		UDPDatagram request;
		String serverName;
		InetAddress serverIp;
		boolean passwordProtected;
		UDPClient client;
		UDPServer listener;

		request = new UDPDatagram(Constants.applicationId.length + SizeConstants.sizeOfByte);

		request.pushByteArray(Constants.applicationId);
		request.pushByte(Constants.identityRequest);

		for (InetAddress broadcastIp : this.broadcasters) {
			client = new UDPClient(Constants.broadcastPort, broadcastIp);
			client.send(request);
			client.close();
		}

		listener = new UDPServer(Constants.broadcastPort, Constants.applicationId.length + Constants.maxIdStringSize + SizeConstants.sizeOfBoolean);

		while ( ( received = listener.receiveExpectedOnTime(Constants.applicationId, Constants.requestResponseTimeLimit, Constants.wrongRequestAnswerLimit) ) != null ) {

			//ignora datagrama enviado ao loopback
			serverIp = received.getSender();
			if (serverIp.equals(this.ip)) {
				continue;
			}
			//Log.d("Client reqid", serverIp.getHostAddress());	//debug

			serverName = received.retrieveString();
			//Log.d("Client reqid", serverName);	//debug
			passwordProtected = received.retrieveBoolean();

			synchronized (Client.this.availableServersLock) {
				Client.this.availableServers.add(new ServerModel(serverName, serverIp, passwordProtected));
			}

		}

		listener.close();

	}

	/*
	 *	Retorna:
	 * 		UDPServer.STATUS_SUCCESSFUL se conexao foi estabelecida
	 * 		UDPServer.STATUS_PACKET_NOT_EXPECTED se senha eh invalida
	 *		UDPServer.STATUS_TIMEOUT se servidor nao respondeu
	 *
	 */
	public static int connectToServer (ServerModel server, String password) throws IOException {

		UDPServer listener;
		UDPClient client;
		int serverPort;
		MacAddress serverMac;
		UDPDatagram request = new UDPDatagram(Constants.applicationId.length + SizeConstants.sizeOfByte + SizeConstants.sizeOfString(password));
		UDPDatagram answer;

		//preparar request
		request.pushByteArray(Constants.applicationId);
		request.pushByte(Constants.connectionRequest);
		request.pushString(password);

		//Log.d("Client connect", server.getAddress().getHostAddress());	//debug
		listener = new UDPServer (Constants.broadcastPort, Constants.applicationId.length + SizeConstants.sizeOfInt + MacAddress.SIZE);
		client = new UDPClient (Constants.broadcastPort, server.getAddress());
		client.send(request);

		do {
			answer = listener.receiveExpectedOnTime(Constants.applicationId, Constants.requestResponseTimeLimit, Constants.wrongRequestAnswerLimit);
			if (answer != null) {
				if ( server.getAddress().equals(answer.getSender()) ) {
					break;
				}
			}
		} while (answer != null);
		if (answer == null) {
			return UDPServer.STATUS_TIMEOUT;
		}

		serverPort = answer.retrieveInt();
		if (serverPort == -1) {
			return UDPServer.STATUS_PACKET_NOT_EXPECTED;
		}

		serverMac = new MacAddress(answer.retrieveByteArray(MacAddress.SIZE, new byte[MacAddress.SIZE], 0), 0);

		server.confirmConnection(serverMac, serverPort, password);
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

	public static void keyboardClick (final ServerModel server, final int keycode) {
		new Thread ( new Runnable() {
			@Override
			public void run() {
				try {
					UDPDatagram command;

					command = Client.prepareCommandDatagram(server, SizeConstants.sizeOfInt);

					command.pushInt(Constants.commandKeyboardClick);
					command.pushInt(keycode);

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
