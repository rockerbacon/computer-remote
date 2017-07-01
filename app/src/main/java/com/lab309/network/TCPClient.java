package com.lab309.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Vitor Andrade dos Santos on 7/1/17.
 */

public class TCPClient {

	/*ATTRIBUTES*/
	private int boundPort;
	private InetAddress boundAddress;
	private Socket sender;

	public TCPClient (int port, InetAddress address) throws IOException {
		this.boundPort = port;
		this.boundAddress = address;
		this.sender = new Socket(this.boundAddress, this.boundPort);
	}


}
