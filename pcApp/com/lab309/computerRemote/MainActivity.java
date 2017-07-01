package com.lab309.computerRemote;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lab309.androidUI.ProgressBarManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

	private Client client;
	private Handler mainHandler;
	private ProgressBarManager progressBar;
	private ArrayList<String> listDisplay;
	private ArrayAdapter<String> listAdapter;

	private void recreateClient () {
		new Thread (new Runnable() {
			@Override
			public void run() {
				try {
					if (MainActivity.this.client == null) {
						MainActivity.this.client = new Client(Build.MODEL);
					} else {
						MainActivity.this.client = new Client(MainActivity.this.client.getName());
					}
					MainActivity.this.refreshAvailableServers();
				} catch (IOException e) {
					//signal no network
					TextView textNoNetwork = (TextView) findViewById(R.id.textNoNetwork);
					textNoNetwork.setVisibility(View.VISIBLE);
					MainActivity.this.client = null;
				}
			}
		}).start();
	}

	private void checkConnections () {
		new Thread (new Runnable () {
			@Override
			public void run() {
				try {
					MainActivity.this.client.checkConnections();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void refreshAvailableServers () {
		this.progressBar.setVisibility(View.VISIBLE);
		new Thread (new Runnable () {
			@Override
			public void run() {
				if (MainActivity.this.client != null) {
					try {

						ServerModel server;

						MainActivity.this.client.clearAvailableServers();
						MainActivity.this.client.requestIdentities();

						MainActivity.this.listDisplay.clear();
						for (int i = 0; i < MainActivity.this.client.getAvailableServersCount(); i++) {
							server = MainActivity.this.client.getAvailableServer(i);
							MainActivity.this.listDisplay.add(server.getName() + " @ " + server.getAddress().getHostAddress());
						}

						MainActivity.this.mainHandler.post(MainActivity.this.updateServerList);
					} catch (IOException e) {
						MainActivity.this.recreateClient();
					}
				}
			}
		}).start();
	}

	private Runnable updateServerList = new Runnable () {
		@Override
		public void run () {

			MainActivity.this.listAdapter.notifyDataSetChanged();
			MainActivity.this.progressBar.setVisibility(View.INVISIBLE);

		}
	};

	@Override
	public void onItemClick (AdapterView<?> l, View v, int position, long id) {
		ServerModel server = this.client.getAvailableServer(position);
		Intent intent = new Intent(this, ConnectActivity.class);

		intent.putExtra("server_name", server.getName());
		intent.putExtra("server_ip", server.getAddress());
		intent.putExtra("server_passwordProtected", server.isPasswordProtected());

		startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.client = null;
		this.mainHandler = new Handler(Looper.getMainLooper());
		this.progressBar = new ProgressBarManager((ProgressBar)findViewById(R.id.progressBar), this.mainHandler);
		this.listDisplay = new ArrayList<String>();
		this.listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, this.listDisplay);

		ListView listAvailableServers = (ListView)findViewById(R.id.listAvailableServers);
		ImageButton buttonRefresh = (ImageButton)findViewById(R.id.buttonRefresh);

		listAvailableServers.setAdapter(this.listAdapter);
		buttonRefresh.setOnClickListener (new View.OnClickListener() {
			@Override
			public void onClick (View v) {
				MainActivity.this.refreshAvailableServers();
			}
		});

	}

	@Override
	protected void onStart() {
		super.onStart();
		try {

			ObjectInputStream clientFile;

			clientFile = new ObjectInputStream( openFileInput(getResources().getString(R.string.file_client)) );
			this.client = (Client)clientFile.readObject();

			this.checkConnections();
			this.refreshAvailableServers();

		} catch (IOException | ClassNotFoundException e) {
			this.recreateClient();
		}
	}

	@Override
	protected void onStop () {
		super.onStop();
		try {
			ObjectOutputStream clientFile;

			clientFile = new ObjectOutputStream ( openFileOutput(getResources().getString(R.string.file_client), MODE_PRIVATE) );
			clientFile.writeObject(this.client);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
