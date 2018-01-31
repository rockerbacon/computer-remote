package com.lab309.security;

import java.security.SecureRandom;

import com.lab309.general.ByteArrayConverter;

import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;

public class RC4Cipher implements Cipher {
	/*ATRIBUTES*/
	private Cipher cipher;
	private KeyGenerator keygen;
	private byte[] publicKey;
	
	/*CONSTRUCTORS*/
	public RC4Cipher (byte[] publicKey) {
		this.cipher = Cipher.getInstance("RC4");
		this.keygen = KeyGenerator.getInstance("RC4");
		this.setKey(publicKey);
	}
	
	public RC4Cipher (int publicKeySize) {
		this.cipher = Cipher.getInstance("RC4");
		this.keygen = KeyGenerator.getInstance("RC4");
		this.publicKey = new byte[publicKeySize];
		s = new SecureRandom();
		s.nextBytes(this.publicKey);
		this.setKey(this.publicKey);
	}
	
	/*GETTERS*/
	public byte[] getKey () {
		byte[] key = new byte[this.publicKey.length];
		return ByteArrayConverter.copyArrayTo(this.publicKey, 0, this.publicKey.length, key, 0);
	}
	
	/*SETTERS*/
	public void setKey (byte[] publicKey) {
		if (this.publicKey != publicKey) {
			ByteArrayConverter.copyArrayTo(publicKey, 0, publicKey.length, this.publicKey, 0);
		}
		this.keygen.init(new SecureRandom(publicKey));
	}
	
	/*METHODS*/
	public byte[] encrypt(byte[] data) {
		if (data == null) return null;
		this.cipher.init(Cipher.ENCRYPT_MODE, keygen.generateKey());
		return this.cipher.doFinal(data);
	}
  
	public byte[] decrypt(byte[] data) {
		if (data == null) return null;
		this.cipher.init(Cipher.DECRYPT_MODE, keygen.generateKey());
		return this.cipher.doFinal(data);
	}
}
