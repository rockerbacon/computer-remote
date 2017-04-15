package com.lab309.network;

import java.io.Serializable;

/**
 * Class for managing a device's macaddress
 *
 * Created by Vitor Andrade dos Santos on 3/21/17.
 */

public class MacAddress implements Serializable {

	public static final int SIZE = 6;

	/*ATTRIBUTES*/
	private byte[] address;

	/*CONSTRUCTORS*/
	public MacAddress (String address) {
		this.address = new byte[MacAddress.SIZE];
		this.setAddress(address);
	}

	public MacAddress (long address) {
		this.address = new byte[MacAddress.SIZE];
		this.setAddress(address);
	}

	public MacAddress (byte[] array, int offset) {
		this.address = new byte[MacAddress.SIZE];
		this.setAddress(array, offset);
	}

	/*GETTERS*/
	public byte[] getAddress () {
		return this.address;
	}

	public String getFormatedAddress (String separator) {
		StringBuilder string = new StringBuilder();
		int i;
		for (i = 0; i < this.address.length; i++) {
			string.append(String.format("%02X", this.address[i]));
			if (i < this.address.length - 1) {
				string.append(separator);
			}
		}
		return new String(string);
	}

	/*SETTERS*/
	public void setAddress (String address) {
		int strCursor;
		char c;

		strCursor = 0;
		for (int i = 0; i < MacAddress.SIZE; i++) {
			this.address[i] = 0;
			for (int j = 0; j < 2; j++) {
				c = address.charAt(strCursor);
				if (c > 47 && c < 58) {
					this.address[i] += (c - 48) * ((j == 0) ? 16 : 1);
				} else if (c > 64 && c < 71) {
					this.address[i] += (c - 55) * ((j == 0) ? 16 : 1);
				} else if (c > 96 && c < 103) {
					this.address[i] += (c - 87) * ((j == 0) ? 16 : 1);
				} else {
					j--;
				}

				strCursor++;
			}

		}
	}

	public void setAddress (long address) {
		for(int i = 0; i < MacAddress.SIZE; i++) {
			this.address[i] = (byte)(address >> i*Byte.SIZE);
		}
	}

	public void setAddress (byte[] address, int offset) {
		for (int i = 0; i < MacAddress.SIZE; i++) {
			this.address[i] = address[offset+i];
		}
	}

	/*METHODS*/
	public byte[] magicPacket() {
		int i, j;
		byte[] packet = new byte[102];

		//header
		for (i = 0; i < 6; i++) {
			packet[i] = (byte)0xFF;
		}

		//macaddress with repetition
		j = 0;
		while (i < 102) {
			packet[i] = this.address[j];
			i++;
			j++;
			if (j == 6) {
				j = 0;
			}
		}

		return packet;
	}

}
