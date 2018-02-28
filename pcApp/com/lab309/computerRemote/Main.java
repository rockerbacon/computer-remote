package com.lab309.computerRemote;

import java.util.Scanner;
import java.util.Random;

import com.lab309.general.ByteArrayConverter;

public class Main {
	public static void main (String[] args) {
		try {

			Scanner wait = new Scanner(System.in);
			
			byte[] password = Server.randomByteArray(Constants.passwordSize);
			byte[] vb = Server.randomByteArray(Constants.validationBytesSize);
			
			Server server = new Server(null, password);

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
