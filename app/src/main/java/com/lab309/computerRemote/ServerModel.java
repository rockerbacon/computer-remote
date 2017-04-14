package com.lab309.computerRemote;

import com.lab309.network.MacAddress;
import com.lab309.network.UDPClient;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by Vitor Andrade dos Santos on 4/13/17.
 */

public class ServerModel {
	/*ATTRIBUTES*/
	private String name;
	private InetAddress ip;
	private boolean passwordProtected;
	private MacAddress mac;
	private UDPClient clientToServer;
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
}
