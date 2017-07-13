package com.lab309.computerRemote;

import java.util.Scanner;

public class Main {
	public static void main (String[] args) {
		try {

			Scanner wait = new Scanner(System.in);
			Server server = new Server ("password");

			System.out.println("Server: " + server.getName() + " @ " + server.getAddress().getHostAddress());
			System.out.println("MacAddress: " + server.getMacAddress().getFormatedAddress("-"));

			server.waitForConnection();

			System.out.println("\nServer is waiting for connections and commands, type \"exit\" to stop waiting");

			while (!wait.nextLine().equals("exit"));

			server.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
