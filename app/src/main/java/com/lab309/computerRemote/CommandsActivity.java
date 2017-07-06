package com.lab309.computerRemote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lab309.network.MacAddress;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CommandsActivity extends AppCompatActivity {

    private EditText commandText;
    private Button sendButton, leftButton, rightButton;

    Intent intent = getIntent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commands);

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

        try
        {
            server.confirmConnection(new MacAddress(intent.getByteArrayExtra("server_mac"), 0), intent.getIntExtra("server_port", -1), intent.getStringExtra("server_pw"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
                        Client.executeLine(server, sender_name, commandText.getText().toString());

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
                        Client.keyboardClick(server, sender_name, 37);
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
                        Client.keyboardClick(server, sender_name, 39);
                    }
                }).start();
            }
        });
    }
}