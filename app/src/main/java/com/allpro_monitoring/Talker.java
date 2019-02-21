package com.allpro_monitoring;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

class  Talker
{
    private static DataOutputStream dos;
    private BufferedReader br;
    String				id;
    Socket              normalSocket;
    Socket              socket;
    //-------file transfer variables below---------

    private OutputStream                os;
    private BufferedInputStream         bis;
    private FileInputStream             fis;
    byte[] 								mybytearray;
    //-------file recieve variables ---------------------
    int portNum;
    int bytesRead;
    int current = 0;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    InputStream is;
    byte []                             mySendbytearray;
    Talker(Socket normalSocket) throws IOException
    {
        this.normalSocket = normalSocket;
        dos = new DataOutputStream(normalSocket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(normalSocket.getInputStream()));
    }
    Talker(String domain, int portNumber,String id) throws UnknownHostException, IOException
    {
        socket = new Socket(domain, portNumber);
        this.id = id;
        dos = new DataOutputStream(socket.getOutputStream());
        br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    static void send(String message) throws IOException
    {
        dos.writeBytes(message + '\n');
        System.out.println ("message sent " + message);
    }
    String recieved() throws IOException
    {
        String message;
        message = br.readLine();
        System.out.println ("message received " + message);
        return message;
    }
    void getId(String id)
    {
        this.id = id;
    }
    //takes a file type as a parameter so it can name the file it recieves. !!@!@!@!@
    void getPics(File file )throws IOException
    {
        try {
            mySendbytearray  = new byte [6022386];
            InputStream is = socket.getInputStream();
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bytesRead = is.read(mybytearray,0,mybytearray.length);
            current = bytesRead;
            System.out.println("WORKING>>>");
            do{
                bytesRead = is.read(mybytearray, current, (mybytearray.length-current));
                if(bytesRead >= 0) current += bytesRead;
            } while(bytesRead > -1);
            bos.write(mybytearray, 0 , current);
            bos.flush();
            System.out.println("File Received" );
        }
        finally {
            if (fos != null)
                fos.close();
            if (bos != null)
                bos.close();
        }
        System.out.println("REACHED END " );
    }

    //sends pics ONLY to server
    public void sendPics(File sendFile) throws Exception {
        try {
            mybytearray = new byte[(int) sendFile.length()];
            fis = new FileInputStream(sendFile);
            bis = new BufferedInputStream(fis);
            bis.read(mybytearray, 0, mybytearray.length);
            os = socket.getOutputStream();
            os.write(mybytearray, 0, mybytearray.length);
            os.flush();
            System.out.println("Sent");
        }
        catch(Exception e ){
            e.printStackTrace();
        }
        finally {
            if (bis != null)
                bis.close();
            if (os != null)
                os.close();
        }
    }
}