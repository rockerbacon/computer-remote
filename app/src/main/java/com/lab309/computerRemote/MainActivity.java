package com.lab309.computerRemote;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Build;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lab309.androidUI.TextViewManager;
import com.lab309.network.UDPServer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

	private Client client;
	private TextViewManager textConnectionStatus;
	private TextViewManager textServerInfo;
	private boolean statusRefreshing;
	private boolean statusConnecting;

	/*METHODS*/
	private void refreshAvailableConnections () {
		if (!MainActivity.this.statusRefreshing) {
			MainActivity.this.statusRefreshing = true;
			new Thread (new Runnable () {
				@Override
				public void run() {
					try {
						int i;
						StringBuilder serversInfo;
						ServerModel server;

						MainActivity.this.client.clearAvailableServers();

						MainActivity.this.textConnectionStatus.setText("Searching for servers");
						MainActivity.this.client.requestIdentities();
						MainActivity.this.textConnectionStatus.setText(MainActivity.this.client.getAvailableServersCount() + " servers available");

						serversInfo = new StringBuilder();
						for (i = 0; i < MainActivity.this.client.getAvailableServersCount(); i++) {

							server = MainActivity.this.client.getAvailableServer(i);

							serversInfo.append('-');
							serversInfo.append(server.getName());
							serversInfo.append(" @ ");
							serversInfo.append(server.getAddress().getHostAddress());
							serversInfo.append('\n');

						}

						MainActivity.this.textServerInfo.setText(new String(serversInfo));

					} catch (IOException e) {
						e.printStackTrace();
						MainActivity.this.textConnectionStatus.setText(e.getMessage());
					} finally {
						MainActivity.this.statusRefreshing = false;
					}
				}
			}).start();
		}
	}

	private void connectToServer (final int index, final String password) {
		if (!MainActivity.this.statusConnecting) {
			MainActivity.this.statusConnecting = true;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						if (MainActivity.this.client.getAvailableServersCount() > 0) {

							int status;
							ServerModel server = MainActivity.this.client.getAvailableServer(index);

							MainActivity.this.textConnectionStatus.setText("Requesting connection to " + server.getName());

							status = MainActivity.this.client.connectToServer(index, password);
							if (status == UDPServer.STATUS_SUCCESSFUL) {
								StringBuilder serverInfo = new StringBuilder();

								MainActivity.this.textConnectionStatus.setText("Connected successfully");

								serverInfo.append("Name: ");
								serverInfo.append(server.getName());
								serverInfo.append('\n');
								serverInfo.append("IP: ");
								serverInfo.append(server.getAddress().getHostAddress());
								serverInfo.append('\n');
								serverInfo.append("Mac address: ");
								serverInfo.append(server.getMacAddress().getFormatedAddress("-"));
								serverInfo.append('\n');

								MainActivity.this.textServerInfo.setText(new String(serverInfo));

							} else if (status == UDPServer.STATUS_PACKET_NOT_EXPECTED) {
								MainActivity.this.textConnectionStatus.setText("Incorrect password");
							} else if (status == UDPServer.STATUS_TIMEOUT) {
								MainActivity.this.textConnectionStatus.setText("Server did not answer in time");
							}


						}
					} catch (IOException e) {
						e.printStackTrace();
						MainActivity.this.textConnectionStatus.setText(e.getMessage());
					} finally {
						MainActivity.this.statusConnecting = false;
					}
				}
			}).start();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onStart() {
		super.onStart();

		this.textConnectionStatus = new TextViewManager( (TextView)findViewById(R.id.textConnectionStatus), new Handler() );
		try {
			Button buttonConnect = (Button)findViewById(R.id.buttonConnect);
			Button buttonRefresh = (Button)findViewById(R.id.buttonRefresh);
			Button buttonCommandHello = (Button)findViewById(R.id.buttonCommandHello);

			this.textServerInfo = new TextViewManager( (TextView)findViewById(R.id.textServerInfo), new Handler() );
			this.client = new Client(Build.MODEL);

			this.statusRefreshing = false;
			this.statusConnecting = false;
			this.textConnectionStatus.setText("Idle");
			this.textServerInfo.setText("No server info available");

			this.refreshAvailableConnections();

			buttonConnect.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					MainActivity.this.connectToServer(0, "this is a password");
				}
			});

			buttonRefresh.setOnClickListener( new View.OnClickListener () {
				@Override
				public void onClick (View v) {
					MainActivity.this.refreshAvailableConnections();
				}
			});

			buttonCommandHello.setOnClickListener (new View.OnClickListener () {
				@Override
				public void onClick (View v) {
					MainActivity.this.client.executeLine(0, "echo Hello");
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
			this.textConnectionStatus.setText(e.getMessage());
		}
	}

	@Override
	protected void onStop () {
		super.onStop();
		this.client.clearConnections();
	}
}
