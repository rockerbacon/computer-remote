package com.lab309.computerRemote;

import com.lab309.network.UDPClient;
import com.lab309.network.UDPServer;
import com.lab309.network.UDPDatagram;

import com.lab309.security.Cipher;
import com.lab309.security.RC4Cipher;

import java.io.Serializable;

import java.io.IOException;
import java.net.InetAddress;

import javax.crypto.IllegalBlockSizeException;

/**
 * Created by Vitor Andrade dos Santos on 4/13/17.
 */

public class ServerModel implements Serializable {
	/*ATTRIBUTES*/
	private InetAddress ip;
	private String name;
	private int connectionPort;
	private byte validationByte;

	private Cipher cipher;
	
	private UDPClient clientToServer;
	private UDPServer feedbackServer;

	/*CONSTRUCTORS*/
	public ServerModel (InetAddress ip, String name, int connectionPort, byte validationByte) {
		this.ip = ip;
		this.name = name;
		this.connectionPort = connectionPort;
		this.validationByte = validationByte;
		this.cipher = null;
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

	public boolean isConnected () {
		return this.clientToServer != null;
	}

	public int getPort() {
		if (this.clientToServer == null) {
			return -1;
		}
		return this.clientToServer.getPort();
	}
	
	public UDPServer getFeedbackServer () {
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
			if (this.cipher == null) this.setKey(key);
			this.clientToServer = new UDPClient(serverPort, this.ip, this.cipher);
			this.feedbackServer = new UDPServer(Constants.maxErrorMessage, this.cipher);
		}

	}

	public void send (UDPDatagram datagram) throws IOException, IllegalBlockSizeException {
		this.clientToServer.send(datagram);
	}

	public void disconnect () {
		this.clientToServer.close();
	}
}
