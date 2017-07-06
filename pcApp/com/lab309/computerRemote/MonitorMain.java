package com.lab309.computerRemote;

import java.util.Scanner;

import java.net.InetAddress;

import java.io.IOException;

public class MonitorMain {
	public static void main (String args[]) {
		Scanner scan = new Scanner(System.in);
		String id;
		Monitor monitor = null;
		
		System.out.println("Type the name or ip of the server you wish to connect to:");
		id = scan.nextLine();
		
		try {
			monitor = new Monitor(InetAddress.getByName(id));
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		System.out.println("Monitor connected, press enter at any time to exit");
		
		scan.nextLine();
		
		monitor.stopLogging();
		
	}
}
