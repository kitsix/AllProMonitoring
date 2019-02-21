package com.allpro_monitoring;


import android.content.Intent;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Button          tabletButton;
    Button          securityCameraButton;
    Button          screenshotButton;
    Button          timelapseButton;
    TextureView     phone1;
    TextureView     phone2;
    TextureView     phone3;
    TextureView     phone4;
    Server          server;
    TextView        infoip, msg;

    //=================================ON CREATE===================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //============================GETTING READY FOR TABLET VIEW===============================
        screenshotButton = findViewById(R.id.screenshotButton);
        screenshotButton.setVisibility(View.INVISIBLE);
        timelapseButton = findViewById(R.id.timelapseButton);
        timelapseButton.setVisibility(View.INVISIBLE);
        phone1 = findViewById(R.id.phone1);
        phone1.setVisibility(View.INVISIBLE);
        phone2 = findViewById(R.id.phone2);
        phone2.setVisibility(View.INVISIBLE);
        phone3 = findViewById(R.id.phone3);
        phone3.setVisibility(View.INVISIBLE);
        phone4 = findViewById(R.id.phone4);
        phone4.setVisibility(View.INVISIBLE);
        tabletButton = findViewById(R.id.tabletButton);
        tabletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchTabletView();
            }
        });
        securityCameraButton = findViewById(R.id.securityCameraButton);
        securityCameraButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                switchClientView();
            }
        });
        //========================================================================================

        infoip = findViewById(R.id.infoip);
        infoip.setVisibility(View.INVISIBLE);
        msg = findViewById(R.id.msg);
        msg.setVisibility(View.INVISIBLE);
    }
//===================================================================================

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        server.onDestroy();
    }

    public void switchTabletView()
    {
        Intent intent = new Intent(this, TabletView.class);
        startActivity(intent);
    }

    public void switchClientView()
    {
        Intent intent = new Intent(this, ClientView.class);
        startActivity(intent);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nightmodeSwitch) {
        }

        else if (id == R.id.securitySwitch) {
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}