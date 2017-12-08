package com.lab309.security;

import java.security.MessageDigest;

public class SHA256Hasher {
	/*ATRIBUTES*/
	private MessageDigest hasher;
	
	/*CONSTRUCTORS*/
	public SHA256Hasher () {
		this.hasher = MessageDigest.getInstance("SHA-256");
	}
	
	/*METHODS*/
	byte[] hash (byte[] data) {
		this.hasher.update(data);
		return this.hasher.digest();
	}	
}
