package com.lab309.computerRemote;

import com.lab309.network.MacAddress;
import com.lab309.network.UDPClient;
import com.lab309.network.UDPDatagram;

import com.lab309.security.RC4Cipher;
import com.lab309.security.SHA256Hasher;

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
	private Cipher cipher;
	private byte validationByte;
	
	private transient UDPClient clientToServer;
	private transient UDPServer feedbackServer;

	/*CONSTRUCTORS*/
	public ServerModel (InetAddress ip, String name, int connectionPort, byte validationByte) {
		this.ip = ip;
		this.name = name;
		this.connectionPort = connectionPort;
		this.cipher = null;
		this.validationByte = validationByte;
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
	
	public boolean isEncrypted () {
		return this.validationByte != 0;
	}
	
	public Cipher getCipher () {
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
	
	public byte getValidationByte () {
		return this.validationByte;
	}
	
	/*SETTERS*/
	public void setKey (byte[] key) {
		this.cipher = new RC4Cipher(key);
	}

	/*METHODS*/
	public void confirmConnection (byte[] key, int serverPort) throws IOException {
		if (this.clientToServer == null) {
			if (this.cipher == null) this.setPassword(key);
			this.clientToServer = new UDPClient(serverPort, this.ip, this.cipher);
			this.feedbackServer = new UDPServer(Constants.maxErrorMessage, this.cipher);
		}

	}

	public void send (UDPDatagram datagram) throws IOException {
		this.clientToServer.send(datagram);
	}

	public void disconnect () {
		this.clientToServer.close();
	}
}
