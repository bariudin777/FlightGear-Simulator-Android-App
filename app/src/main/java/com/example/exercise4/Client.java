package com.example.exercise4;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Client Class
 * <<<COMMUNICATION WITH SIMULATOR>>>
 * Have 3 members: path,socket,outStream
 * path - contains the path and the value that we send
 * socket- communication between user and simulator
 * outStream- tool to send data
 */
public class Client {

    private Map<String, String> paths;
    private Socket socket;
    private OutputStream outStream;

    /**
     * Client constructor
     * <<<Initialize the path member>>>
     */
    public Client() {
        this.paths = new HashMap<>();
        this.paths.put("AILERON", "/controls/flight/aileron");
        this.paths.put("ELEVATOR", "/controls/flight/elevator");
    }

    /**
     * Connect
     * <<<Connect the user to simulator>>>
     * Gets the ip and port from user, and establish the connection with the simulator
     * If one of the parameters are wrong- throws exception
     *
     * @param ip   the ip of the socket
     * @param port the port of the socket
     * @throws IOException the io exception
     */
    public void connect(String ip, int port) throws IOException {
        InetAddress serverAddr = InetAddress.getByName(ip);
        System.out.println("Connecting...");
        this.socket = new Socket();
        //Wait until the connection are done.
        //if there is a problem- throws time-out
        this.socket.connect(new InetSocketAddress(serverAddr, port),15*1000);
        System.out.println("Connected");
        this.outStream = this.socket.getOutputStream();
    }

    /**
     * Send Command
     * <<<Send the data to inner class>>>
     * Gets the params and sends them to doBackGround method of the inner class
     *
     * @param parameter the parameter
     * @param value     the value
     */
    public void sendCommand(String parameter, String value) {
        new SendCommandTask().execute(parameter, value);
    }

    /**
     * Disconnect
     * <<<Disconnect the communication>>>
     */
    public void disconnect() {
        try {
            this.outStream.close();
            this.socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * SendCommandTask Class
     * <<<SENDING DATA TO SIMULATOR>>>
     * Inner class that extends AsyncTask<String, Void, Void>
     * @Override - doInBackGround method
     *
     */
    private class SendCommandTask extends AsyncTask<String, Void, Void> {

        /**
         * doInBackground Command
         * <<<Send the data to Simulator>>>
         * Gets Parameters of type- String array
         * Proses the data (parameter,value) and member- paths
         * Sends the data to simulator with outStream member
         * @return null
         *
         */
        @Override
        protected Void doInBackground(String... strings) {
            String parameter = strings[0].toUpperCase();
            String value = strings[1];
            if (!paths.containsKey(parameter)) {
                return null;
            }
            String msg = "set " + paths.get(parameter) + " " + value + " \r\n";
            byte[] command = msg.getBytes();

            try {
                outStream.write(command, 0, command.length);
                outStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}