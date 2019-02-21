package com.allpro_monitoring;

import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class TabletView extends AppCompatActivity implements View.OnClickListener
{

    Button          tabletButton;
    Button          securityCameraButton;
    Button          screenshotButton;
    Button          timelapseButton;
    TextureView     phone1;
    TextureView     phone2;
    TextureView     phone3;
    TextureView     phone4;
    TextView        infoip, msg;
    Server          server;

    //=================================ON CREATE===================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        server = new Server(this);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        tabletButton = findViewById(R.id.tabletButton);
        tabletButton.setVisibility(View.INVISIBLE);
        securityCameraButton = findViewById(R.id.securityCameraButton);
        securityCameraButton.setVisibility(View.INVISIBLE);

        screenshotButton = findViewById(R.id.screenshotButton);
        screenshotButton.setVisibility(View.VISIBLE);
        screenshotButton.setOnClickListener(this);
        timelapseButton = findViewById(R.id.timelapseButton);
        timelapseButton.setVisibility(View.VISIBLE);
        phone1 = findViewById(R.id.phone1);
        phone1.setVisibility(View.VISIBLE);
        phone2 = findViewById(R.id.phone2);
        phone2.setVisibility(View.VISIBLE);
        phone3 = findViewById(R.id.phone3);
        phone3.setVisibility(View.VISIBLE);
        phone4 = findViewById(R.id.phone4);
        phone4.setVisibility(View.VISIBLE);

        msg = findViewById(R.id.msg);
        msg.setVisibility(View.VISIBLE);
        infoip = findViewById(R.id.infoip);
        infoip.setVisibility(View.VISIBLE);
        infoip.setText(server.getIpAddress() + ":" + server.getPort());
        // shows ip and current port on server

    }




    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.screenshotButton:
                //need to use the servers talker to call the getPics() method
                try {

                    server.talker.send("PICTURE");

                }

                catch(IOException x)
                {
                    x.printStackTrace();
                }


        }// end switch

    }// end onClick
}
//===================================================================================