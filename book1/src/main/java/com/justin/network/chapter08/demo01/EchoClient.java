package com.justin.network.chapter08.demo01;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class EchoClient {
    private String remoteHost = "localhost";
    private int remotePort = 8000;
    private DatagramSocket socket;

    public EchoClient() throws IOException {
        socket = new DatagramSocket();
    }

    public static void main(String args[]) throws IOException {
        new EchoClient().talk();
    }

    public void talk() throws IOException {
        try {
            InetAddress remoteIP = InetAddress.getByName(remoteHost);

            BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));
            String msg = null;
            while ((msg = localReader.readLine()) != null) {
                byte[] outputData = msg.getBytes();
                DatagramPacket outputPackage = new DatagramPacket(outputData, outputData.length, remoteIP, remotePort);
                socket.send(outputPackage);

                DatagramPacket inputPacket = new DatagramPacket(new byte[512], 512);
                socket.receive(inputPacket);
                System.out.println(new String(inputPacket.getData(), 0, inputPacket.getLength()));

                if (msg.equals("bye")) break;
            }
        } catch(IOException ex) {
            ex.printStackTrace();;
        } finally {
            socket.close();
        }
    }
}

