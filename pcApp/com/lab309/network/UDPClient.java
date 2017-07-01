package com.lab309.network;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Class for sending UDP packets
 *
 * Created by Vitor Andrade dos Santos on 3/27/17.
 */
public class UDPClient {

		/*ATTRIBUTES*/
		private int boundPort;
		private InetAddress boundAddress;
		private DatagramSocket sender;


		/*CONSTRUCTOR*/
		public UDPClient (int port, InetAddress address) throws IOException {
			this.boundPort = port;
			this.boundAddress = address;
			this.sender = new DatagramSocket();
		}

		/*GETTERS*/
		public InetAddress getAddress () {
			return this.boundAddress;
		}
		public int getPort () {
			return this.boundPort;
		}

		/*METHODS*/
		public void send (UDPDatagram datagram) throws IOException {
			this.sender.send( new DatagramPacket(datagram.buffer, datagram.offset, this.boundAddress, this.boundPort) );
		}

		public void close () {
			this.sender.close();
		}
}
