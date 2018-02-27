package com.lab309.computerRemote;

import android.content.Context;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lab309.computerRemote.R;
import com.lab309.general.ByteArrayConverter;
import com.lab309.network.UDPServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ConnectActivity extends AppCompatActivity {
	private EditText password;
	private Button sendButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect);

		Intent intent = getIntent();

        final Client client = (Client)intent.getSerializableExtra("client");

        Log.d("Number of servers:", " "+client.getAvailableServersCount());
        final ServerModel server = client.getAvailableServer(intent.getIntExtra("serverIndex", 0));

        if (!server.isEncrypted()) {
            try {
                client.connectToServer(server, null);
            } catch (IOException e) {
                Log.d("ERROR_CONNECTTOSERVER", "Erro ao conectar ao servidor");
                e.printStackTrace();
            }
        } else {
				//mostrar caixa de texto requisitando senha
                password = (EditText) findViewById(R.id.txtPassword);
                sendButton = (Button) findViewById(R.id.button_send_pw);

                sendButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
						//Log.d("CLICKMALDITO", "CLIQUEI, PORRAAAAAAAAA");
						new Thread(new Runnable() { @Override public void run () {
							try {
								byte[] b = ByteArrayConverter.fromStringRepresentation(password.getText().toString());
								//Log.d("Sending password", ByteArrayConverter.toStringRepresentation(b));
								if (client.connectToServer(server, ByteArrayConverter.fromStringRepresentation(password.getText().toString())) == UDPServer.STATUS_SUCCESSFUL) {
									Intent intent = new Intent(ConnectActivity.this, CommandsActivity.class);

									intent.putExtra("server_name", server.getName());
									intent.putExtra("server_address", server.getAddress());
									intent.putExtra("server_passwordProtected", server.isEncrypted());
									startActivity(intent);
								} else {
									ConnectActivity.this.runOnUiThread( new Runnable () { @Override public void run() {
										Context context = getApplicationContext();
										Toast.makeText(context, "Invalid password", Toast.LENGTH_LONG).show();
									}});
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}}).start();
					}
                });
			}
	}
}
