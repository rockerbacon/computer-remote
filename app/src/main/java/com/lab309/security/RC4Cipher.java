package com.lab309.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.lab309.general.ByteArrayConverter;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;

public class RC4Cipher implements com.lab309.security.Cipher {
	/*ATRIBUTES*/
	private Cipher encryptor;
	private Cipher decryptor;
	private byte[] publicKey;
	
	/*CONSTRUCTORS*/
	public RC4Cipher (byte[] publicKey) {
		try {
			this.encryptor = Cipher.getInstance("RC4");
			this.decryptor = Cipher.getInstance("RC4");
			this.setKey(publicKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public RC4Cipher (int publicKeySize) {
		try {
			SecureRandom s;

			this.encryptor = Cipher.getInstance("RC4");
			this.decryptor = Cipher.getInstance("RC4");
			this.publicKey = new byte[publicKeySize];

			s = new SecureRandom();
			s.nextBytes(this.publicKey);
			this.setKey(this.publicKey);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*GETTERS*/
	public byte[] getKey () {
		return ByteArrayConverter.copyArrayTo(this.publicKey, 0, this.publicKey.length, new byte[this.publicKey.length], 0);
	}
	
	/*SETTERS*/
	public void setKey (byte[] publicKey) throws InvalidKeyException {
		try {
			KeyGenerator keygen = KeyGenerator.getInstance("RC4");
			if (this.publicKey != publicKey) {
				ByteArrayConverter.copyArrayTo(publicKey, 0, publicKey.length, this.publicKey, 0);
			}
			keygen.init(new SecureRandom(publicKey));
			this.encryptor.init(Cipher.ENCRYPT_MODE, keygen.generateKey());
			this.decryptor.init(Cipher.DECRYPT_MODE, keygen.generateKey());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	/*METHODS*/
	@Override
	public byte[] encrypt(byte[] data) throws IllegalBlockSizeException {
		try {
			if (data == null) return null;
			return this.encryptor.doFinal(data);
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException {
		if (data == null) return null;
		return this.decryptor.doFinal(data);
	}
}
