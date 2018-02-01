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
	private InetAddress ip;
	private String name;
	private int connectionPort;
	private boolean passwordProtected;
	private RC4Cipher cipher;
	
	private transient UDPClient clientToServer;
	private transient UDPServer feedbackServer;
	private String password;

	/*CONSTRUCTORS*/
	public ServerModel (InetAddress ip, String name, int connectionPort, boolean passwordProtected, byte[] publicKey) {
		this.ip = ip;
		this.name = name;
		this.connectionPort = connectionPort;
		this.passwordProtected = passwordProtected;
		this.cipher = null;
		if (publicKey != null) this.cipher = new RC4Cipher (publicKey);
	}

	/*GETTERS*/
	public InetAddress getAddress () {
		return this.ip;
	}
	
	public String getName () {
		return this.name;
	}
	
	public int getConnectionPort() {
		return this.connectionPort;
	}
	
	public boolean isPasswordProtected () {
		return this.passwordProtected;
	}
	
	public RC4Cipher getCipher () {
		return this.cipher;
	}

	public String getPassword () {
		return this.password;
	}

	public boolean isConnected () {
		return this.clientToServer != null;
	}

	public int getPort() {
		if (this.clientToServer == null) {
			return -1;
		}
		return this.clientToServer.getPort();
	}
	
	public int getFeedbackServer () {
		return this.feedbackServer;
	}

	/*METHODS*/
	public void confirmConnection (int serverPort, String password) throws IOException {
		if (this.clientToServer == null) {
			this.clientToServer = new UDPClient(serverPort, this.ip, this.cipher);
			this.feedbackServer = new UDPServer(Constants.maxErrorMessage, this.cipher);
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
