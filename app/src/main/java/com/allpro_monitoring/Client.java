package com.allpro_monitoring;

import java.io.File;
import android.annotation.SuppressLint;
import android.os.Environment;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.AsyncTask;
import android.widget.TextView;

public class Client extends AsyncTask<Void, Void, Void> implements Runnable {

    int             portNumber;
    TextView        textResponse;
    ClientView      clientView;
    Talker          talker;
    String          cmd = "";
    String          photoDir;
    String          ipAddress;
    String          response = "";

    //=======================================================================
    //=======================================================================

    Client(ClientView clientView, String addr, int port, TextView textResponse)
    {
        this.clientView = clientView;
        ipAddress = addr;
        portNumber = port;
        this.textResponse = textResponse;

    }

    //=======================================================================
    //=======================================================================

    Client(ClientView clientView)
    {
        this.clientView = clientView;
    }

    //=======================================================================
    //=======================================================================

    //doInBackground is possible through extending AsyncTask, which allows easy
    //access to the UI thread this way we do not have to use other threads or
    //handlers
    @Override
    protected Void doInBackground(Void... arg0)
    {
        Socket socket = null;

        try
        {
            //Create a socket using the specified IP address and port number.
            talker = (new Talker(ipAddress, portNumber, "user"));
            new Thread(this).start();
        }

        catch (UnknownHostException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        }

        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "IOException: " + e.toString();
        }

        return null;
    }

    public void run()
    {

        while (true)
        {
            try
            {

                cmd = talker.recieved();


                if (cmd.startsWith("PICTURE"))//protocol for sending pics ;)
                    sendPics();

                //if command doesnt equal nothing then post it to the gui, thread safe obv :)
                clientView.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!cmd.equals(""))
                            clientView.response.setText(cmd);

                        else if (cmd.equals("PICTURE"))
                            sendPics();
                    }
                });

            } catch (Exception e)

            {
                System.out.println("Exception caught in the run() method of Client.java..");
                e.printStackTrace();
            }


        }

    }


    private String getGalleryPath()
    {
        return photoDir = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/";
    }

    public void listFilesForFolder(final File folder)
    {
        for (final File fileEntry : folder.listFiles())
        {
            if (fileEntry.isDirectory())
            {
                listFilesForFolder(fileEntry);
            }
            else
                {
                System.out.println(fileEntry.getName());
            }
        }
    }

    //code to send pictures to server
    @SuppressLint("SetWorldReadable")
    private void sendPics()
    {
        //File file = new File(
        // Environment.getExternalStorageDirectory(),
        // "IMG_20190217_005442.png");

        //File file = new File("/Phone/DCIM/Camera/IMG_20190217_005442.jpg");

        //File file = Environment.getExternalStoragePublicDirectory(
        //Environment.DIRECTORY_DCIM);

        photoDir = getGalleryPath();
        System.out.println("DIRECTORY" + photoDir);


        File filex = new File(photoDir);
        listFilesForFolder(filex);

        //File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //String xpath = "/storage/emulated/0/DCIM/Camera";
        //File file = new File(xpath, "IMG_20190211_163701.jpg");
        File file = new File("/Phone/DCIM/Camera/IMG_20190206_185011.jpg");
        file.setReadable(true);


        try
        {
            talker.sendPics(file);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}