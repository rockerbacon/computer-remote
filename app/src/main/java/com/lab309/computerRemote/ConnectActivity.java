package com.lab309.computerRemote;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lab309.network.UDPServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ConnectActivity extends AppCompatActivity
{
    private EditText password;
    private Button sendButton;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        Intent intent = getIntent();
        final String sender_name = intent.getStringExtra("sender_name");

        ServerModel serv = null;

        try
        {
            serv = new ServerModel(intent.getStringExtra("server_name"), InetAddress.getByAddress(intent.getByteArrayExtra("server_address")), intent.getBooleanExtra("server_passwordProtected", false));
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }

        final ServerModel server = serv;

        if(!server.isPasswordProtected())
        {
            try
            {
                Client.connectToServer(server, "");
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            password = (EditText) findViewById(R.id.txtPassword);
            password.getBackground().setColorFilter(Color.parseColor("#000000"), PorterDuff.Mode.SRC_IN);

            sendButton = (Button) findViewById(R.id.button_send_pw);

            sendButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    final Handler h = new Handler()
                    {
                        public void handleMessage(Message msg)
                        {
                            if(msg.what == 0)
                            {
                                Toast.makeText(getApplicationContext(), "Senha Inválida!", Toast.LENGTH_LONG).show();
                            }
                        }
                    };

                    new Thread (new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                if( Client.connectToServer(server, password.getText().toString()) == UDPServer.STATUS_SUCCESSFUL )
                                {
                                    Intent intent = new Intent(ConnectActivity.this, CommandsActivity.class);

                                    intent.putExtra("server_name", server.getName());
                                    intent.putExtra("server_address", server.getAddress().getAddress());
                                    intent.putExtra("server_passwordProtected", server.isPasswordProtected());
                                    intent.putExtra("server_mac", server.getMacAddress().getAddress());
                                    intent.putExtra("server_port", server.getPort());
                                    intent.putExtra("server_pw", password.getText().toString());
                                    intent.putExtra("sender_name", sender_name);
                                    startActivity(intent);
                                }
                                else
                                {
                                    h.sendEmptyMessage(0);
                                }
                            }
                            catch(IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            });
        }
    }
}
