package com.lab309.computerRemote;

import java.net.InetAddress;

import com.lab309.network.UDPClient;
import com.lab309.network.UDPServer;
import com.lab309.network.UDPDatagram;
import com.lab309.network.MacAddress;
import com.lab309.network.NetInfo;

import com.lab309.general.SizeConstants;

import com.lab309.os.Terminal;

import java.io.IOException;
import java.net.SocketException;

/*
 *	Classe Server eh responsavel por receber e executar comandos.
 *	Servidor possui um UDPServer conectado a uma porta de broadcasting, fixa e determinada em Constants.broadcastPort, por onde recebe solicitacoes de conexao (Server.broadcastServer).
 *	Servidor possui um UDPServer conectado a uma porta qualquer que estaja disponivel no momento da instanciacao do objeto da classe por onde recebe comandos a serem executados (Server.commandsServer).
 *	Servidor possui um protocolo de handshaking:
 *		1-Cliente envia packet pela porta Constants.broadcastPort para o ip de broadcasting da rede contendo, nessa ordem:
 *			1.1-String Constants.applicationId;
 *			1.2-byte Constants.identityRequest ou String Constants.connectionRequest;
 *			1.3-Se byte em 1.3 == Constants.connectionRequest:
 *					-String password;
 *		2-Servidor recebe o packet enviado em 1 e envia packet pela porta Constants.broadcastPort para o IP do cliente contendo, nessa ordem:
 *			2.1-String Constants.applicationId;
 *			2.2-Se byte em 1.2 == Constants.identityRequest:
 *					-String Server.name;
 *					Se Server.password != "":
 *						-boolean true
 *					Senao
 *						-boolean false
 *				Senao se byte em 1.2 == Constants.connectionRequest:
 *					Se String em 1.3 == Server.password || Server.password == "":
 *						-int contendo porta de Server.commandsServer;
 *						-MacAddress do servidor;
 *					Senao:
 *						-int contendo o valor -1;
 *
 *	Para recebimento de comandos serve o seguinte protocolo
 *		1-Cliente envia packet pela porta de Server.commandsServer para o ip do servidor, contendo:
 *			1.1-MacAddress do servidor;
 *			1.2-String contendo nome do cliente
 *			1.3-int contendo o id do comando (vide classe Constants);
 *			1.4-data adicional para o comando (vide classe Constants, esse campo eh referenciado como "campo de data");
 *		2-Se Server.mac == MacAddress em 1.1:
 *			-Servidor executa comando;
 *
 */
public class Server {

	/*ATTRIBUTES*/
	private String password;
	private String name;
	private InetAddress ip;
	private MacAddress mac;

	private UDPServer broadcastServer;
	private UDPServer commandsServer;
	private boolean waitingForConnection;

	private Terminal terminal;

	/*CONSTRUCTORS*/
	public Server (String password) throws IOException {
 
		this.password = password;
		this.name = InetAddress.getLocalHost().getHostName();
		//System.out.println(this.name);	//debug
		this.ip = NetInfo.thisMachineIpv4();
		this.mac = new MacAddress(NetInfo.machineMacByIp(this.ip), 0);

		//this.broadcastServer = new UDPServer (Constants.broadcastPort, Constants.broadcastBufferSize);
		this.commandsServer = new UDPServer (Constants.commandBufferSize);

		this.waitingForConnection = false;

		this.terminal = new Terminal(System.out);

		this.waitForCommand();
	}

	/*GETTERS*/
	public String getName() {
		return name;
	}

	public InetAddress getAddress() {
		return ip;
	}

	public MacAddress getMacAddress() {
		return mac;
	}

	/*METHODS*/
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
								} else {
									//empacota porta invalida
									port.pushInt(-1);
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

	public void stopWaitingForConnection () {
		if (this.waitingForConnection) {
			this.broadcastServer.close();
			this.broadcastServer = null;
			this.waitingForConnection = false;
		}
	}

	private void waitForCommand () throws IOException {
		new Thread ( new Runnable () {
			@Override
			public void run () {

				try {

					UDPDatagram received;
					String clientName;
					int command;

					while (true) {

						received = Server.this.commandsServer.receiveExpected(Server.this.mac.getAddress());

						clientName = received.retrieveString();
						command = received.retrieveInt();

						switch (command) {
							case Constants.commandExecuteLine:
								String line = received.retrieveString();
								Server.this.terminal.execute(clientName + '@' + received.getSender().getHostAddress(), line);
							break;
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

	public void close () {
		this.stopWaitingForConnection();
		this.commandsServer.close();
	}

}
