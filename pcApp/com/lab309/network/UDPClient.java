package com.lab309.network;

import java.io.IOException;

import com.lab309.security.Cipher;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.crypto.IllegalBlockSizeException;

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
		private Cipher cipher;


		/*CONSTRUCTOR*/
		public UDPClient (int port, InetAddress address, Cipher cipher) throws IOException {
			this.boundPort = port;
			this.boundAddress = address;
			this.sender = new DatagramSocket();
			this.cipher = cipher;
		}

		/*GETTERS*/
		public InetAddress getAddress () {
			return this.boundAddress;
		}
		public int getPort () {
			return this.boundPort;
		}
		
		/*SETTERS*/
		public void setCipher (Cipher cipher) {
			this.cipher = cipher;
		}

		/*METHODS*/
		public void send (UDPDatagram datagram) throws IOException, IllegalBlockSizeException {
			if (this.cipher != null) {
				byte[] message = this.cipher.encrypt(datagram.getBuffer().getByteArray());
				this.sender.send( new DatagramPacket(message, message.length, this.boundAddress, this.boundPort) );
			} else {
				this.sender.send( new DatagramPacket(datagram.getBuffer().getByteArray(), datagram.getBuffer().getOffset(), this.boundAddress, this.boundPort) );
			}	
		}

		public void close () {
			this.sender.close();
		}
}
