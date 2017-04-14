package com.lab309.computerRemote;

import com.lab309.network.MacAddress;
import com.lab309.network.NetInfo;
import com.lab309.network.UDPClient;
import com.lab309.network.UDPServer;
import com.lab309.network.UDPDatagram;

import com.lab309.general.SizeConstants;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by Vitor Andrade dos Santos on 4/13/17.
 */

public class Client {

	/*ATTRIBUTES*/
	private InetAddress ip;
	private InetAddress broadcastIp;
	private ArrayList<ServerModel> availableServers;
	private ArrayList<ServerModel> connectedServers;
	private final Object availableServersLock = new Object();
	private final Object connectedServersLock = new Object();

	/*CONSTRUCTORS*/
	public Client () throws IOException {
		this.ip = NetInfo.thisMachineIpv4();
		this.broadcastIp = NetInfo.broadcastIp();
		if (broadcastIp == null || this.ip == null) {
			throw new IOException("Could not connect to network");
		}
		this.availableServers = new ArrayList<ServerModel>();
		this.connectedServers = new ArrayList<ServerModel>(5);
	}

	/*GETTERS*/
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

		listener = new UDPServer(Constants.broadcastPort, Constants.applicationId.length + Constants.maxIdStringSize + SizeConstants.sizeOfBoolean);
		client = new UDPClient(Constants.broadcastPort, this.broadcastIp);
		client.send(request);

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
		client.close();

	}

	/*
	 *	Retorna:
	 * 		UDPServer.STATUS_SUCCESSFUL se conexao foi estabelecida
	 * 		UDPServer.STATUS_PACKET_NOT_EXPECTED se senha eh invalida
	 *		UDPServer.STATUS_TIMEOUT se servidor nao respondeu
	 *
	 */
	public int connectToServer (int index, String password) throws IOException {

		UDPServer listener;
		UDPClient client;
		ServerModel server;
		int serverPort;
		MacAddress serverMac;
		UDPDatagram request = new UDPDatagram(Constants.applicationId.length + SizeConstants.sizeOfByte + SizeConstants.sizeOfString(password));
		UDPDatagram answer;

		server = this.availableServers.get(index);

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
		synchronized (this.connectedServersLock) {
			this.connectedServers.add(server);
		}

		return UDPServer.STATUS_SUCCESSFUL;

	}

	public void clearAvailableServers () {
		synchronized (this.availableServersLock) {
			this.availableServers.clear();
		}
	}
}
