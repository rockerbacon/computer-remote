package com.lab309.network;

import com.lab309.general.ByteArrayConverter;
import com.lab309.general.SizeConstants;

import java.net.InetAddress;

public class UDPDatagram {

	/*ATTRIBUTES*/
	byte[] buffer;
	int offset;
	private InetAddress sender;
	private int sentPort;

	/*CONSTRUCTORS*/
	UDPDatagram (byte[] buffer, int offset, InetAddress sender, int sentPort) {
		this.buffer = buffer;
		this.offset = offset;
		this.sender = sender;
		this.sentPort = sentPort;
	}
	public UDPDatagram (byte[] buffer, int offset) {
		this.buffer = buffer;
		this.offset = offset;
	}
	public UDPDatagram (int bufferSize) {
		this(new byte[bufferSize], 0);
	}
	public UDPDatagram (byte[] buffer) {
		this(buffer, 0);
	}

	/*GETTERS*/
	public InetAddress getSender() {
		return this.sender;
	}

	public int getSentPort() {
		return this.sentPort;
	}

	/*METHODS*/
	/*PUSH DATA*/
	public void pushByte (byte b) {
		this.buffer[this.offset] = b;
		this.offset++;
	}

	public void pushShort (short hd) {
		ByteArrayConverter.shortToArray(hd, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfShort;
	}

	public void pushInt (int d) {
		ByteArrayConverter.intToArray(d, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfInt;
	}

	public void pushLong (long ld) {
		ByteArrayConverter.longToArray(ld, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfLong;
	}

	public void pushFloat (float f) {
		ByteArrayConverter.floatToArray(f, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfFloat;
	}

	public void pushDouble (double lf) {
		ByteArrayConverter.doubleToArray(lf, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfDouble;
	}

	public void pushBoolean (boolean b) {
		ByteArrayConverter.booleanToArray(b, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfBoolean;
	}

	public void pushChar (char c) {
		ByteArrayConverter.charToArray(c, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfChar;
	}

	public void pushLatinChar (char c) {
		ByteArrayConverter.latinCharToArray(c, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfLatinChar;
	}

	public void pushString (String string) {
		ByteArrayConverter.stringToArray(string, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfString(string);
	}

	public void pushLatinString (String string) {
		ByteArrayConverter.latinStringToArray(string, this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfLatinString(string);
	}

	public void pushByteArray (byte[] b, int beginning, int end) {
		ByteArrayConverter.copyArrayTo(b, beginning, end, this.buffer, this.offset);
		this.offset += (end - beginning > 0) ? end - beginning : 0; 
	}

	public void pushByteArray (byte[] b) {
		this.pushByteArray(b, 0, b.length);
	}

	/*RETRIEVE DATA*/
	public byte retrieveByte () {
		byte b = this.buffer[this.offset];
		this.offset++;
		return b;
	}

	public short retrieveShort () {
		short hd = ByteArrayConverter.shortFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfShort;
		return hd;
	}

	public int retrieveInt () {
		int d = ByteArrayConverter.intFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfInt;
		return d;
	}

	public long retrieveLong () {
		long ld = ByteArrayConverter.longFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfLong;
		return ld;
	}

	public float retrieveFloat () {
		float f = ByteArrayConverter.floatFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfFloat;
		return f;
	}

	public double retrieveDouble () {
		double lf = ByteArrayConverter.doubleFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfDouble;
		return lf;
	}

	public boolean retrieveBoolean () {
		boolean b = ByteArrayConverter.booleanFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfBoolean;
		return b;
	}

	public char retrieveChar () {
		char c = ByteArrayConverter.charFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfChar;
		return c;
	}

	public char retrieveLatinChar () {
		char c = ByteArrayConverter.latinCharFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfLatinChar;
		return c;
	}

	public String retrieveString () {
		String string = ByteArrayConverter.stringFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfString(string);
		return string;
	}

	public String retrieveLatinString () {
		String string = ByteArrayConverter.latinStringFromArray(this.buffer, this.offset);
		this.offset += SizeConstants.sizeOfLatinString(string);
		return string;
	}

	public byte[] retrieveByteArray (int size, byte[] output, int outputOffset) {
		ByteArrayConverter.copyArrayTo(this.buffer, this.offset, this.offset+size, output, outputOffset);
		this.offset += (size > 0) ? size : 0;
		return output;
	}
}
