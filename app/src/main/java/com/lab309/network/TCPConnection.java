package com.lab309.network;

import java.util.Scanner;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Classe para envio e recebimento de dados em uma conexao
 *
 * Created by Vitor Andrade dos Santos on 7/1/17.
 */

public class TCPClient {

	/*ATTRIBUTES*/
	private Socket connection;
	private DataOutputStream outputStream;
	private Scanner inputStream;

	/*METHODS*/
	public TCPClient(int port, InetAddress address, String mode) throws IOException {
		this.connection = new Socket(address, port);
		this.outputStream = new DataOutputStream(this.connection.getOutputStream());
		this.inputStream = new Scanner(this.connection.getInputStream());
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

	public void sendString (String str) throws IOException {
		this.outputStream.writeChars(str);
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
	public
}
