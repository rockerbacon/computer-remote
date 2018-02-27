package com.lab309.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.lab309.general.ByteBuffer;
import com.lab309.security.Cipher;

import com.lab309.general.ByteArrayConverter;

import java.io.IOException;
import java.net.SocketTimeoutException;

import java.io.Serializable;

/**
 * Class for receiving UDP packets
 *
 * Created by Vitor Andrade dos Santos on 3/18/17.
 */
public class UDPServer implements Serializable {

	/*STATIC CONSTANTS*/
	public static final int	STATUS_SUCCESSFUL = 0,
							STATUS_PACKET_NOT_EXPECTED = 1,
							STATUS_TIMEOUT = 2;

	/*ATTRIBUTES*/
	private transient DatagramSocket receiver;
	private transient DatagramPacket bufferPacket;
	private InetAddress boundAddress;
	private Cipher cipher;


	/*CONSTRUCTOR*/
	public UDPServer (int port, int bufferSize, InetAddress address, Cipher cipher) throws IOException {
		this.receiver = new DatagramSocket(port);
		this.bufferPacket = new DatagramPacket (new byte[bufferSize], bufferSize);
		this.boundAddress = address;
		this.cipher = cipher;
	}

	public UDPServer (int port, int bufferSize, Cipher cipher) throws IOException {
		this(port, bufferSize, null, cipher);
	}

	public UDPServer (int bufferSize, Cipher cipher) throws IOException {
		this.receiver = new DatagramSocket();
		this.bufferPacket = new DatagramPacket(new byte[bufferSize], bufferSize);
		this.boundAddress = null;
		this.cipher = cipher;
	}

	/*GETTERS*/
	public int getPort () {
		return this.receiver.getLocalPort();
	}

	public InetAddress getLastSenderAddress () {
		return this.bufferPacket.getAddress();
	}

	public int getLastSenderPort () {
		return this.bufferPacket.getPort();
	}

	public int getBufferSize () { return this.bufferPacket.getData().length; }
	
	/*SETTERS*/
	public void setCipher (Cipher cipher) {
		this.cipher = cipher;
	}

	/*METHODS*/
	public UDPDatagram receive () throws IOException {

		byte[] data;
		
		do {
			this.receiver.receive(this.bufferPacket);
		} while ( this.boundAddress != null && !this.bufferPacket.getAddress().equals(this.boundAddress) );
		
		if (this.cipher != null) {
			try {
				data = this.cipher.decrypt(this.bufferPacket.getData());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			data = this.bufferPacket.getData();
		}

		return new UDPDatagram(new ByteBuffer(data), this.bufferPacket.getAddress(), this.bufferPacket.getPort());
	}

	//retorna null se nenhum datagrama foi recebido a tempo
	public UDPDatagram receiveOnTime (int timeInMillis, int limitOfTries) throws IOException {

		byte[] data;
		this.receiver.setSoTimeout(timeInMillis);

		try {

			do {
				if (limitOfTries == 0) {
					return null;
				}
				this.receiver.receive(this.bufferPacket);
				limitOfTries--;

			} while ( this.boundAddress != null && !this.bufferPacket.getAddress().equals(this.boundAddress) );

		} catch (SocketTimeoutException e) {
			return null;
		} finally {
			this.receiver.setSoTimeout(0);
		}
		
		if (this.cipher != null) {
			try {
				data = this.cipher.decrypt(this.bufferPacket.getData());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			data = this.bufferPacket.getData();
		}
		
		return new UDPDatagram(new ByteBuffer(data), this.bufferPacket.getAddress(), this.bufferPacket.getPort());
	}

	public UDPDatagram receiveOnTime (int timeInMillis) throws IOException {
		return this.receiveOnTime(timeInMillis, 1);
	}

	//waits for a package with the first bytes matching "expected" to be received
	//returns a datagram offseted to after the expected bytes
	public UDPDatagram receiveExpected (byte[] expected) throws IOException {
		byte[] data;

		do {
			this.receiver.receive(this.bufferPacket);
			
			if (this.cipher != null) {
				try {
					data = this.cipher.decrypt(this.bufferPacket.getData());
				} catch (Exception e) {
					e.printStackTrace();
					data = null;
				}
			} else {
				data = this.bufferPacket.getData();
			}

			if (data == null) {
				continue;
			}
			
			System.out.println ("Received packet from " + this.bufferPacket.getAddress().toString() + " containing:");	//debug
			System.out.println (ByteArrayConverter.toStringRepresentation(data)+"\n");	//debug
			

			for (int i = 0; i < data.length && i < expected.length; i++) {
				if (data[i] != expected[i]) {
					data = null;
					break;
				}
			}

		} while ( data == null || this.boundAddress != null && !this.bufferPacket.getAddress().equals(this.boundAddress) );

		return new UDPDatagram(new ByteBuffer(data, expected.length), this.bufferPacket.getAddress(), this.bufferPacket.getPort());
	}

	//null se pacote esperado nao foi recebido
	public UDPDatagram receiveExpectedOnTime (byte[] expected, int timeInMillis, int limitOfTries) throws IOException {
		byte[] data;

		this.receiver.setSoTimeout(timeInMillis);

		try {

			do {

				if (limitOfTries == 0) {
					return null;
				}
				this.receiver.receive(this.bufferPacket);
				limitOfTries--;
				
				if (this.cipher != null) {
					try {
						data = this.cipher.decrypt(this.bufferPacket.getData());
					} catch (Exception e) {
						e.printStackTrace();
						data = null;
					}
				} else {
					data = this.bufferPacket.getData();
				}

				if (data == null) {
					continue;
				}

				for (int i = 0; i < data.length && i < expected.length; i++) {
					if (data[i] != expected[i]) {
						data = null;
						break;
					}
				}

			} while ( data == null || this.boundAddress != null && !this.bufferPacket.getAddress().equals(this.boundAddress) );

			return new UDPDatagram(new ByteBuffer(data, expected.length), this.bufferPacket.getAddress(), this.bufferPacket.getPort());

		} catch (SocketTimeoutException e) {
			return null;
		} finally {
			this.receiver.setSoTimeout(0);
		}
	}

	public void close () {
		this.receiver.close();
	}


	private void writeObject (ObjectOutputStream output) throws IOException {
		output.defaultWriteObject();
		output.writeInt(this.getPort());
		output.writeInt(this.getBufferSize());
	}

	private void readObject (ObjectInputStream input) throws IOException, ClassNotFoundException {
		int bufferSize;
		input.defaultReadObject();
		this.receiver = new DatagramSocket(input.readInt());
		bufferSize = input.readInt();
		this.bufferPacket = new DatagramPacket(new byte[bufferSize], bufferSize);
	}

}
