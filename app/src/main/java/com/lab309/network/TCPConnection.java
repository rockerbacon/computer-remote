package com.lab309.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Classe para envio e recebimento de dados em uma conexao TCP
 *
 * Created by Vitor Andrade dos Santos on 7/1/17.
 */

public class TCPConnection {

	/*ATTRIBUTES*/
	protected DataOutputStream outputStream;
	protected DataInputStream inputStream;

	/*METHODS*/
	public TCPConnection () {}
	public TCPConnection (DataOutputStream outputStream, DataInputStream inputStream) {
		this.setStreams(outputStream, inputStream);
	}
	protected void setStreams (DataOutputStream outputStream, DataInputStream inputStream) {
		this.outputStream = outputStream;
		this.inputStream = inputStream;
	}

	/*SEND*/
	public void sendBytes( byte[] b, int off, int len) throws IOException {
		this.outputStream.write(b, off, len);
	}

	public void sendByte (byte b) throws IOException {
		this.outputStream.writeByte(b);
	}

	public void sendChar (char c) throws IOException {
		this.outputStream.writeChar(c);
	}

	public void sendLatinChar (char c) throws IOException {
		this.outputStream.writeByte((byte)c);
	}

	public void sendLatinString (String str) throws IOException {
		int i;
		for (i = 0; i < str.length(); i++) {
			this.outputStream.writeByte((byte)str.charAt(i));
		}
		this.outputStream.writeByte((byte)0);
	}

	public void sendString (String str) throws IOException {
		for (int i = 0; i < str.length(); i++) {
			this.outputStream.writeChar(str.charAt(i));
		}
		this.outputStream.writeChar((char)0);
	}

	public void sendDouble (double lf) throws IOException {
		this.outputStream.writeDouble(lf);
	}

	public void sendFloat (float f) throws IOException {
		this.outputStream.writeFloat(f);
	}

	public void sendInt (int d) throws IOException {
		this.outputStream.writeInt(d);
	}

	public void sendLong (long ld) throws IOException {
		this.outputStream.writeLong(ld);
	}

	public void sendShort (short hd) throws IOException {
		this.outputStream.writeShort(hd);
	}

	public void sendBoolean (boolean b) throws IOException {
		this.outputStream.writeBoolean(b);
	}

	/*RECEIVE*/
	public byte[] readBytes(byte[] b, int off, int len) throws IOException {
		this.inputStream.read(b, off, len);
		return b;
	}

	public boolean readBoolean() throws IOException {
		return this.inputStream.readBoolean();
	}

	public byte readByte() throws IOException {
		return this.inputStream.readByte();
	}

	public char readChar() throws IOException {
		return this.inputStream.readChar();
	}

	public char readLatinChar() throws IOException {
		return (char)(0xFF & this.inputStream.readByte());
	}

	public String readLatinString() throws IOException {
		StringBuilder string = new StringBuilder();
		char c;

		c = (char)(0xFF & this.inputStream.readByte());
		while (c != 0) {
			string.append(c);
			c = (char)(0xFF & this.inputStream.readByte());
		}

		return new String(string);
	}

	public String readString() throws IOException {
		StringBuilder string = new StringBuilder();
		char c;

		c = this.inputStream.readChar();
		while (c != 0) {
			string.append(c);
			c = this.inputStream.readChar();
		}

		return new String(string);
	}

	public void readDouble() throws IOException {
		this.inputStream.readDouble();
	}

	public void readFloat() throws IOException {
		this.inputStream.readFloat();
	}

	public void readInt() throws IOException {
		this.inputStream.readInt();
	}

	public void readLong() throws IOException {
		this.inputStream.readLong();
	}

	public void readShort() throws IOException {
		this.inputStream.readShort();
	}

}
