package com.lab309.computerRemote;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity {

	private Client client;
	private Handler mainHandler;
	private ProgressBarManager progressBar;
	private ArrayList<String> listDisplay;
	private ArrayAdapter<String> listAdapter;

	private Runnable updateServerList = new Runnable () {
		@Override
		public void run () {

			MainActivity.this.listAdapter.notifyDataSetChanged();
			MainActivity.this.progressBar.setVisibility(View.INVISIBLE);

		}
	};

	private AdapterView.OnItemClickListener connectionClick = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick (AdapterView<?> l, View v, int position, long id) {
			Intent intent = new Intent(MainActivity.this, ConnectActivity.class);

			intent.putExtra("client", MainActivity.this.client);
			intent.putExtra("serverIndex", position);

			/*
			intent.putExtra("server_name", server.getName());
			intent.putExtra("server_address", server.getAddress().getAddress());
			intent.putExtra("server_passwordProtected", server.isEncrypted());
			*/

			startActivity(intent);
		}
	};

	private void refreshAvailableServers () {
		TextView textNoNetwork = (TextView)findViewById(R.id.textNoNetwork);
		this.progressBar.setVisibility(View.VISIBLE);
		textNoNetwork.setVisibility(View.INVISIBLE);
		new Thread (new Runnable () {
			@Override
			public void run() {
				if (MainActivity.this.client != null) {
					try {

						ServerModel server;

						MainActivity.this.client.clearAvailableServers();
						MainActivity.this.client.searchServers();

						MainActivity.this.listDisplay.clear();
						for (int i = 0; i < MainActivity.this.client.getAvailableServersCount(); i++) {
							server = MainActivity.this.client.getAvailableServer(i);
							MainActivity.this.listDisplay.add(server.getName() + " @ " + server.getAddress().getHostAddress());
						}

						MainActivity.this.mainHandler.post(MainActivity.this.updateServerList);

						MainActivity.this.progressBar.setVisibility(View.INVISIBLE);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
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
		listAvailableServers.setOnItemClickListener(this.connectionClick);
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
			this.client = new Client(Build.MODEL);
			this.refreshAvailableServers();
		} catch (IOException e) {
			//signal no network
			TextView textNoNetwork = (TextView) findViewById(R.id.textNoNetwork);
			textNoNetwork.setVisibility(View.VISIBLE);
			this.client = null;
		}

	}

	@Override
	protected void onStop () {
		super.onStop();
		/*
		try {
			ObjectOutputStream clientFile;

			clientFile = new ObjectOutputStream ( openFileOutput(getResources().getString(R.string.file_client), MODE_PRIVATE) );
			clientFile.writeObject(this.client);

		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
}
