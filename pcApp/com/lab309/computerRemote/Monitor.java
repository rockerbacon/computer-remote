package com.lab309.computerRemote;

import com.lab309.TCPClient;

public class Monitor {
	/*ATTRIBUTES*/
	private TCPClient logClient;
	
	/*CONSTRUCTORS*/
	public Monitor (InetAddress address) {
		this.logClient = new TCPClient(Constants.broadcastPort, address);
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
				} catch (IOException) {
					System.out.println("Could not establish connection");
				}
			}
		}).start();
	}
}
