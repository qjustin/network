package com.justin.network.chapter08.demo03;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class SendChannel {
    public static void main(String args[]) throws Exception {
        DatagramChannel channel = DatagramChannel.open();
        DatagramSocket socket = channel.socket();
        SocketAddress localAddr = new InetSocketAddress(7000);
        SocketAddress remoteAddr = new InetSocketAddress(InetAddress.getByName("localhost"), 8000);
        socket.bind(localAddr);

        while(true) {
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.clear();
            System.out.println("Remaining bytes.." + buffer.remaining());
            int n = channel.send(buffer, remoteAddr);
            System.out.println("Sended bytes.." + n);
            Thread.sleep(500);
        }
    }
}
