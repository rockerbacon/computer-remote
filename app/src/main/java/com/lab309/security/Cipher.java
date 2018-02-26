package com.lab309.security;

import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public interface Cipher {
	public byte[] encrypt (byte[] data) throws IllegalBlockSizeException;
	public byte[] decrypt (byte[] data) throws IllegalBlockSizeException, BadPaddingException;
	public byte[] getKey ();
	public void setKey (byte[] key) throws InvalidKeyException;
}
