package com.lab309.computerRemote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CommandsActivity extends AppCompatActivity {

    private EditText commandText;
    private Button sendButton, leftButton, rightButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commands);

        Intent intent = getIntent();

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

        if(server == null)
        {
            Log.d("SERVIDOR NULOOOOOO!!", "SREREREVERRR NULOOOOOOOOO");
        }
        commandText = (EditText) findViewById(R.id.txtCommands);
        sendButton = (Button) findViewById(R.id.button_send_cmd);
        leftButton = (Button) findViewById(R.id.button_cmd_left);
        rightButton = (Button) findViewById(R.id.button_cmd_right);

        sendButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                new Thread (new Runnable()
                {
                    @Override
                    public void run()
                    {
                        String s = commandText.getText().toString();
                        if(s != null)
                        {
                            Client.executeLine(server, s);
                        }
                        else
                        {
                            Log.d("AAAAAAAAAAHAHA", "STRING NULAAAAAAAAA!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        }
                    }
                }).start();
            }
        });

        leftButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                new Thread (new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Client.keyboardClick(server, 37);
                    }
                }).start();
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                new Thread (new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Client.keyboardClick(server, 39);
                    }
                }).start();
            }
        });
    }
}