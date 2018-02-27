package com.lab309.computerRemote;

import java.util.Scanner;
import java.util.Random;

import java.security.SecureRandom;

import com.lab309.general.ByteArrayConverter;

public class Main {
	public static void main (String[] args) {
		try {

			Scanner wait = new Scanner(System.in);
			
			SecureRandom rnd = new SecureRandom();
			
			byte[] password = new byte[3];
			byte vb = (byte)(new Random().nextInt());
			
			rnd.nextBytes(password);
			
			Server server = new Server(vb, null, password);

			System.out.println("Server: " + server.getName() + " @ " + server.getAddress().getHostAddress());
			System.out.println("Password: " + ByteArrayConverter.toStringRepresentation(password));

			System.out.println("\nServer is waiting for connections and commands, type \"exit\" to stop waiting");

			while (!wait.nextLine().equals("exit"));

			server.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
