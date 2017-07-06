package com.lab309.computerRemote;

import com.lab309.network.TCPClient;

import java.net.InetAddress;

import java.io.IOException;

public class Monitor {
	/*ATTRIBUTES*/
	private TCPClient logClient;
	
	/*CONSTRUCTORS*/
	public Monitor (InetAddress address) {
		try {
			this.logClient = new TCPClient(Constants.broadcastPort, address);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.log();
	}
	
	public void log () {
		new Thread (new Runnable() {
			@Override
			public void run () {
				String line;
				try {
					while (true) {
						line = Monitor.this.logClient.readString();
						System.out.print(line);
					}
				} catch (IOException e) {
					System.out.println("Could not establish connection");
				}
			}
		}).start();
	}
	
	public void stopLogging() {
		this.logClient.close();
	}
}
