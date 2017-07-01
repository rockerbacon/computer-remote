package com.lab309.computerRemote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lab309.computerRemote.R;

import java.io.IOException;
import java.net.InetAddress;

public class ConnectActivity extends AppCompatActivity {
	private EditText password;
	private Button sendButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_connect);



		Intent intent = getIntent();
		try {
			final ServerModel server = new ServerModel(intent.getStringExtra("server_name"), InetAddress.getByAddress(intent.getByteArrayExtra("server_address")), intent.getBooleanExtra("server_passwordProtected", false));

			if (!server.isPasswordProtected()) {
				Client.connectToServer(server, "");
			} else {
				//mostrar caixa de texto requisitando senha
                password = (EditText) findViewById(R.id.txtPassword);
                sendButton = (Button) findViewById(R.id.button_send_pw);

                sendButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v){
                        try
                        {
                            Client.connectToServer(server, password.getText().toString());
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
