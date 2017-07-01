package com.lab309.network;

import java.io.DataOutputStream;
import java.io.DataInputStream;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Vitor Andrade dos Santos on 7/1/17.
 */

public class TCPServer extends TCPConnection {
	/*ATTRIBUTES*/
	private Socket connection;

	/*CONSTRUCTORS*/
	public TCPServer () {}
	public TCPServer (int port) throws IOException {
		this.connect(port);
	}

	/*METHODS*/
	public void connect( int port) throws IOException {
		this.connection = (new ServerSocket(port)).accept();
		this.setConnection(this.connection);
	}
}
