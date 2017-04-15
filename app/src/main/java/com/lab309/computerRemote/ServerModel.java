package com.lab309.computerRemote;

import com.lab309.network.MacAddress;
import com.lab309.network.UDPClient;
import com.lab309.network.UDPDatagram;

import java.io.Serializable;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Vitor Andrade dos Santos on 4/13/17.
 */

public class ServerModel implements Serializable {
	/*ATTRIBUTES*/
	private String name;
	private InetAddress ip;
	private boolean passwordProtected;
	private MacAddress mac;
	private transient UDPClient clientToServer;
	private String password;

	/*CONSTRUTORS*/
	public ServerModel (String name, InetAddress ip, boolean passwordProtected) {
		this.name = name;
		this.ip = ip;
		this.passwordProtected = passwordProtected;
		this.mac = null;
		this.clientToServer = null;
		this.password = null;
	}

	/*GETTERS*/
	public String getName () {
		return this.name;
	}

	public InetAddress getAddress () {
		return this.ip;
	}

	public MacAddress getMacAddress () {
		return this.mac;
	}

	public String getPassword () {
		return this.password;
	}

	public boolean isPasswordProtected () {
		return this.passwordProtected;
	}

	public boolean isConnected () {
		return this.clientToServer != null;
	}

	/*METHODS*/
	public void confirmConnection (MacAddress mac, int serverPort, String password) throws IOException {
		if (this.clientToServer == null) {
			this.mac = mac;
			this.clientToServer = new UDPClient(serverPort, this.ip);
			this.password = password;
		}

	}

	public void send (UDPDatagram datagram) throws IOException {
		this.clientToServer.send(datagram);
	}

	public void disconnect () {
		this.clientToServer.close();
	}
}
