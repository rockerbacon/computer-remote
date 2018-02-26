package com.lab309.security;

public interface Cipher {
	public byte[] encrypt (byte[] data);
	public byte[] decrypt (byte[] data);
	public byte[] getKey ();
	public void setKey (byte[] key);
}
