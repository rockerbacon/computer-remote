package com.lab309.computerRemote;

import java.net.InetAddress;
import java.util.LinkedList;
import java.awt.Robot;

import com.lab309.network.UDPClient;
import com.lab309.network.UDPServer;
import com.lab309.network.UDPDatagram;
import com.lab309.network.TCPServer;
import com.lab309.network.MacAddress;
import com.lab309.network.NetInfo;

import com.lab309.steward.CommandsQueue;
import com.lab309.steward.Steward;

import com.lab309.general.SizeConstants;

import java.security.SecureRandom;

import java.io.IOException;
import java.net.SocketException;

/*
 *	Class for managing connections and receiving commands (but not executing them).
 *	Server has an UDPServer connected to the breadcast port specified in Constants.broadcastPort, from where it receives connection requests (Server.broadcastServer).
 *	Server has an UDPServer connected to a random port available at the moment of the object's instantiation from where it receives commands to be executed.
 *	Server uses RC4 for security
 *
 *	The connection protocol goes as follows:
 *		1-Client broadcasts a packet to the broadcast port, having: (String appId = Constants.applicationId, byte request, String password)
 *
 *		2-Server receives the packet and answers to the client's IP through the broadcast port:
 *			2.1-If request == Constants.identityRequest: (String serverName, boolean isPasswordProtected, byte[] publicKey)
 *			2.2-If request == Constants.connectionRequest:
 *					If password == Server.password || Server.password == "": (int commandsPort, byte[] publicKey)
 *					Else: (int commandsPort = -1)
 *			See Constants.publicKeySize for the size of the public key
 *
 *	The transmission of commands follows the protocol:
 *		3-Client sends packet throught the port informed in 2.2 to the server's IP, having: (String clientName, int commandId, ... args) 
 *			For the arguments of each type of command see class Steward
 *
 */
public class Server {

	/*ATTRIBUTES*/
	private Steward steward;
	
	private String password;
	private String name;
	private InetAddress ip;
	
	private UDPServer broadcastServer;
	private UDPServer commandsServer;
	private TCPServer logServer;
	
	private CommandsQueue commandQueue;
	private boolean processingCommands;
	private Object signal;
	
	private byte[] publicKey;
	
	private boolean waitingForConnection;
	
	/*THREADS*/
	
	/*CONSTRUCTORS*/
	public Server (String password) throws IOException {
 		SecureRandom s;
 		this.steward = new Steward();
 		
		this.password = password;
		this.name = InetAddress.getLocalHost().getHostName();
		//System.out.println(this.name);	//debug
		this.ip = NetInfo.thisMachineIpv4();

		this.commandsServer = new UDPServer (Constants.commandBufferSize);
		this.commandQueue = new ConcurrentStaticQueue<UDPDatagram> (Constants.commandQueueSize);
		this.signal = new Object();
		
		this.publicKey = new byte[Constants.publicKeySize];
		s = new SecureRandom();
		s.nextBytes(this.publicKey);
		
		this.logServer = new TCPServer(Constants.broadcastPort);
		new Thread( new Runnable () {
			@Override
			public void run () {
				try {
					Server.this.logServer.connect();
				} catch (SocketException e) {
					System.out.println (e.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		this.waitingForConnection = false;

		this.waitForCommands();
		new Thread(this.processCommands).start();
	}

	/*GETTERS*/
	public String getName() {
		return name;
	}

	public InetAddress getAddress() {
		return ip;
	}

	/*METHODS*/
	private void log (final String message) {
		new Thread (new Runnable() {
			@Override
			public void run() {
				if (Server.this.logServer != null) {
					if (Server.this.logServer.isConnected()) {
						try {
							Server.this.logServer.sendString(message+'\n');
						} catch (IOException e) {
							//caso nao consiga enviar mensagem fecha conexao atual e espera nova conexao
							Server.this.logServer.close();
							try {
								Server.this.logServer = new TCPServer(Constants.broadcastPort);
								Server.this.logServer.connect();
							} catch (SocketException e1) {
								System.out.println (e1.getMessage());
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			}	
		}).start();
	}
	
	public void waitForConnection () throws IOException {
		if (!this.waitingForConnection) {
			this.broadcastServer = new UDPServer(Constants.broadcastPort, Constants.broadcastBufferSize);
			this.waitingForConnection = true;

			new Thread ( new Runnable () {
				@Override
				public void run () {

					UDPDatagram received;

					try {

						while (Server.this.broadcastServer != null) {

							//espera anuncio de conexao
							received = Server.this.broadcastServer.receiveExpected(Constants.applicationId);

							//verifica se packet eh destinado a aplicacao
							UDPClient client;
							InetAddress connectingDeviceIp;
							byte request;

							//criar client para ip do disposivo que quer se conectar
							connectingDeviceIp = received.getSender();
							request = received.retrieveByte();
							client = new UDPClient(Constants.broadcastPort, connectingDeviceIp);

							if (request == Constants.identityRequest) {

								UDPDatagram name = new UDPDatagram(Constants.applicationId.length + SizeConstants.sizeOfString(Server.this.name) + SizeConstants.sizeOfBoolean);

								//preparar datagrama para envio
								name.pushByteArray(Constants.applicationId);
								name.pushString(Server.this.name);
								name.pushBoolean (!Server.this.password.equals(""));

								client.send(name);
								
								Server.this.log("Client @ " + connectingDeviceIp.getHostAddress() + " requested server's identity");

							} else if (request == Constants.connectionRequest) {

								UDPDatagram port = new UDPDatagram(Constants.applicationId.length + SizeConstants.sizeOfInt + MacAddress.SIZE);
								String password;

								password = received.retrieveString();

								port.pushByteArray(Constants.applicationId);
								//confirma ou recusa conexao
								if (password.equals(Server.this.password) || Server.this.password.length() == 0) {
									//empacota porta e mac address para envio
									port.pushInt(Server.this.commandsServer.getPort());
									port.pushByteArray(Server.this.mac.getAddress(), 0, MacAddress.SIZE);
									Server.this.log("Client @ " + connectingDeviceIp.getHostAddress() + " connected to server");
								} else {
									//empacota porta invalida
									port.pushInt(-1);
									Server.this.log("Client @ " + connectingDeviceIp.getHostAddress() + " typed wrong password");
								}

								client.send(port);
								client.close();
					
							}

						}

					} catch (SocketException e) {
						System.out.println (e.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();

		}
	}
	
	private void waitForCommands () {
		new Thread ( new Runnable () {
				@Override
				public void run () {
					try {
						UDPDatagram received;

						while (true) {
							received = Server.this.commandsServer.receiveExpected(Server.this.mac.getAddress());
							if (Server.this.commandQueue.length() < Constants.commandQueueSize) {
								Server.this.commandQueue.push(received);
								synchronized (Server.this.signal) { Server.this.signal.notify(); }
							}
						}
					} catch (SocketException e) {
						System.out.println(e.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
	}

	public void stopWaitingForConnection () {
		if (this.waitingForConnection) {
			this.broadcastServer.close();
			this.broadcastServer = null;
			this.waitingForConnection = false;
		}
	}

	public void close () {
		this.stopWaitingForConnection();
		this.commandsServer.close();
		this.logServer.close();
		this.processingCommands = false;
		synchronized (Server.this.signal) { Server.this.signal.notifyAll(); }
	}

}

