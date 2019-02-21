package com.allpro_monitoring;

import android.content.Intent;
import android.view.View.OnClickListener;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;



public class ClientView extends AppCompatActivity implements OnClickListener {

    private static final String LOG_TAG_EXTERNAL_STORAGE = "EXTERNAL_STORAGE";

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;

    String TAG = ClientView.class.getSimpleName();

    Client          client;
    TextView        response;
    EditText        editTextAddress, editTextPort;
    Button          buttonConnect, buttonClear;
    String[]        permissions;
    ActivityCamera  cameraActivity;

    //=======================================================================
    //=======================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_view);

        client = new Client(this);

        //Completing the GUI - ability to access
        editTextAddress = findViewById(R.id.addressEditText);
        editTextPort = findViewById(R.id.portEditText);
        buttonConnect = findViewById(R.id.connectButton);
        buttonConnect.setOnClickListener(this);
        buttonClear = findViewById(R.id.clearButton);
        buttonClear.setOnClickListener(this);
        response = findViewById(R.id.responseTextView);






    }
    //=======================================================================
    //=======================================================================


    //acts as an actionPerformed method in which the button pressed can be
    //identified and the current class can be passed to the constructor of
    //the Client class which will attempt to connect to the server.




    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {

            case R.id.connectButton:
                System.out.println("The connect button has ben pressed.....");
                //When the button is pressed, parse the input and execute the Client Class
                Client myClient = new Client(this, editTextAddress.getText()
                        .toString(), Integer.parseInt(editTextPort
                        .getText().toString()), response);
                myClient.execute();



            case R.id.clearButton:
                System.out.println("The camera button has been pressed....");
                Intent switchToCamera = new Intent(ClientView.this, ActivityCamera.class);
                startActivity(switchToCamera);


        }


    }




   /*
    public void connect(View view)
    {
        System.out.println("The connect button has ben pressed.....");
        //When the button is pressed, parse the input and execute the Client Class
        Client myClient = new Client(this, editTextAddress.getText()
                .toString(), Integer.parseInt(editTextPort
                .getText().toString()), response);
        myClient.execute();
    }
    */




   /*
    public void openCamera(View view)
    {
        System.out.println("The camera button has been pressed...");
        Intent switchToCamera = new Intent(ClientView.this, ActivityCamera.class);
        startActivity(switchToCamera);
    }
*/

}

