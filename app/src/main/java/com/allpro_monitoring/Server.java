package com.allpro_monitoring;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class Server
{
    TabletView          activity;                          //main class for server
    ServerSocket        serverSocket;
    String              message = "";                            //string from user
    static final int    socketServerPORT = 8080;       //port is hard coded
    private Bitmap      bitmap;
    Talker              talker;

    //constructor
    public Server(TabletView activity)
    {
        this.activity = activity;

        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    //------end constructor--------------
    //always returns the port? which is weird bc its hard coded as 8080
    public int getPort() {
        return socketServerPORT;
    }

    //closes the socket and ends connection
    public void onDestroy() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    //this class accepts new clients, the amount of which is stored in count, every time a new one is accepted count increments
// important to note it doesnt decrement on a client leaving how ever it is unsure if that matters.
    private class SocketServerThread extends Thread {
        Talker  talker;
        int count = 0; // number of clients i have

        @Override
        public void run()
        {
            try
            {
                // create ServerSocket using specified port
                serverSocket = new ServerSocket(socketServerPORT);

                while (true)
                {

                    Socket socket = serverSocket.accept();




                    //******************************
                    //******************************
                    talker =  new Talker(socket);
                    new SocketServerReplyThread(talker, count++);
                    //new SocketServerReplyThread(new Talker(socket), count++);

                    message += "#" + count + " from "                                                               //message from client
                            + socket.getInetAddress() + ":"                                                          //this line shows ip
                            + socket.getPort() + "\n";                                                               //and port of client, not really needed in final client

                    activity.runOnUiThread(new Runnable() {                                                         //thread safe gui update, not needed if above message isnt, just used for testing
                        @Override
                        //this inner class isnt needed
                        public void run() {
                            activity.msg.setText(message);
                        }
                    });                                                                                             //similar to java

                }
            } catch (IOException e) {
                System.out.println("IOException in server socket, did not connect");
                e.printStackTrace();
            }
        }
    }

    //thread that replies are handled on, thread is started in above SocketServerThread, inside a while so it continously runs and recieves, while connected
    private class SocketServerReplyThread implements Runnable
    {

        Talker talker;
        int cnt;//amount of clients connected to server
        String message;

        SocketServerReplyThread(Talker talker, int c)
        {
            this.talker = talker;                                                              //using clients socket to make output streams, needs input to recieve messages
            cnt = c;                                                                                //number of clients
            System.out.println("Inside communcation thread");
            new Thread(this).start();
        }

        @Override
        public void run()
        {



            try
            {
                Talker.send("HELLO you are now connected to the server :)");

                while (true)
                {
                    message = talker.recieved();                                                //server waits for sent message from
                    System.out.println("in the try while true within server . java");

                    if (message.startsWith("PICTURE"))
                        receivePicture();
                    else if (message.startsWith("text"))
                        activity.runOnUiThread(new Runnable()
                        {

                            @Override
                            public void run()
                            {
                                activity.msg.setText(message + " This is inside message protocol!!");                                                  //wrapper to set messages
                            }
                        });

                }


            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            }
//inner class that updates gui with a message, only if the catch is activated,
            activity.runOnUiThread(new Runnable()
            {

                @Override
                public void run() {
                    activity.msg.setText(message + " only seee this if picture didnt transmit!!!");                                                  //wrapper to set messages
                }
            });
        }

        // sets the file name as to what it will save as, then calls the talker to get it from the stream
        private void receivePicture() throws IOException
        {
            System.out.println("Made it into the recieve picture method within the server******");
            File file = new File(Environment.getExternalStorageDirectory(), "test.png");
            talker.getPics(file);
        }
//------------------------------------------------------------------------------
    }//ends server socket reply thread

    //return ip address of server
    public String getIpAddress()
    {
        String ip = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        }
        catch (SocketException ex)
        {
            ex.printStackTrace();

        }
        return null;
    }
}