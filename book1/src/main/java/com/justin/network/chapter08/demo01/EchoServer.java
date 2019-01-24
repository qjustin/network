package com.justin.network.chapter08.demo01;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class EchoServer {
    private int port = 8000;
    private DatagramSocket socket;

    public EchoServer() throws IOException {
        socket = new DatagramSocket(port);
        System.out.println("Server started");
    }

    public void service() {
        while(true) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[512], 512);
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                System.out.println(packet.getAddress() + ":" + packet.getPort() + ":" + msg);
                packet.setData(msg.getBytes());
                socket.send(packet);
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) throws IOException {
        new EchoServer().service();
    }
}
