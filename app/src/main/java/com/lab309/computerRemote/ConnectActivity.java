package com.lab309.computerRemote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;

import com.lab309.computerRemote.R;

import java.io.IOException;
import java.net.InetAddress;

public class ConnectActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect);

		Intent intent = getIntent();
		try {
			ServerModel server = new ServerModel(intent.getStringExtra("server_name"), InetAddress.getByAddress(intent.getByteArrayExtra("server_address")), intent.getBooleanExtra("server_passwordProtected", false));

			if (!server.isPasswordProtected()) {
				Client.connectToServer(server, "");
			} else {
				//mostrar caixa de texto requisitando senha
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
