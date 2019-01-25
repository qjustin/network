package com.justin.network.chapter08.demo04;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;

public class EchoServer {
    private int port = 8000;
    private DatagramChannel channel;
    private final int MAX_SIZE = 1024;

    public EchoServer() throws IOException {
        channel = DatagramChannel.open();
        DatagramSocket socket = channel.socket();
        SocketAddress localAddr = new InetSocketAddress(8000);
        socket.bind(localAddr);
        System.out.println("Server started");
    }

    public String echo(String msg) {
        return "echo:" + msg;
    }

    public void service() {
        ByteBuffer receiveBuffer = ByteBuffer.allocate(MAX_SIZE);
        while(true) {
            try {
                receiveBuffer.clear();
                // 接收来自任意一个EchoClient的数据报
                InetSocketAddress client = (InetSocketAddress)channel.receive(receiveBuffer);
                receiveBuffer.flip();
                String msg = Charset.forName("GBK").decode(receiveBuffer).toString();
                System.out.println(client.getAddress() + ":" + client.getPort() + ">" + msg);
                // 回复EchoClient
                channel.send(ByteBuffer.wrap(echo(msg).getBytes()), client);
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String args[]) throws IOException {
        new EchoServer().service();
    }
}
