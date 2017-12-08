package com.lab309.security;

import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;

public class RC4Cipher {
	/*ATRIBUTES*/
	private Cipher cipher;
	private KeyGenerator keygen;
	private byte[] publicKey;
	
	/*CONSTRUCTORS*/
	public RC4Cipher (byte[] publicKey) {
		this.setKey(publicKey);
	}
	
	public RC4Cipher (int publicKeySize) {
		this.publicKey = new byte[publicKeySize];
		s = new SecureRandom();
		s.nextBytes(this.publicKey);
		this.setKey(this.publicKey);
	}
	
	/*SETTERS*/
	public void setKey (byte[] publicKey) {
		this.cipher = Cipher.getInstance("RC4");
		this.keygen = KeyGenerator.getInstance("RC4");
		this.keygen.init(new SecureRandom(publicKey));
	}
	
	/*METHODS*/
	public byte[] encrypt(byte[] data) {
		this.cipher.init(Cipher.ENCRYPT_MODE, keygen.generateKey());
		return this.cipher.doFinal(data);
	}
  
	public byte[] decrypt(byte[] data) {
		this.cipher.init(Cipher.DECRYPT_MODE, keygen.generateKey());
		return this.cipher.doFinal(data);
	}
}
