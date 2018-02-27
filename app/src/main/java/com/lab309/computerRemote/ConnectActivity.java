package com.lab309.computerRemote;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lab309.computerRemote.R;
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

        ServerModel serv = null;

        try {
            serv = new ServerModel(InetAddress.getByAddress(intent.getByteArrayExtra("server_address")), intent.getStringExtra("server_name"), intent.getIntExtra("server_connectionPort", -1), intent.getByteExtra("server_vb", (byte)0));
        } catch (UnknownHostException e) {
            Log.d("ERROR_SERVERMODEL","Erro ao criar ServerModel");
            e.printStackTrace();
        }

        final ServerModel server = serv;

        if (!server.isEncrypted()) {
            /*
            try {
                //Client.connectToServer(server, "");
            } catch (IOException e) {
                Log.d("ERROR_CONNECTTOSERVER", "Erro ao conectar ao servidor");
                e.printStackTrace();
            }
            */
        } else {
				//mostrar caixa de texto requisitando senha
                password = (EditText) findViewById(R.id.txtPassword);
                sendButton = (Button) findViewById(R.id.button_send_pw);

                sendButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v){
                        Log.d("CLICKMALDITO", "CLIQUEI, PORRAAAAAAAAA");
                        try
                        {
                            if( /*Client.connectToServer(server, password.getText().toString()) == UDPServer.STATUS_SUCCESSFUL*/ true )
                            {
                                Intent intent = new Intent(ConnectActivity.this, CommandsActivity.class);

                                intent.putExtra("server_name", server.getName());
                                intent.putExtra("server_address", server.getAddress());
                                intent.putExtra("server_passwordProtected", server.isEncrypted());
                                Log.d("PASSOUUUU", "Passou no if");
                                startActivity(intent);
                            }
                            else
                            {
                                Log.d("PASSOUUUUNAAAAO", "Passou no ELSE");
                                Context context = getApplicationContext();
                                Toast toast = Toast.makeText(context, "Senha inválida. Tente novamente.", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
			}
	}
}
