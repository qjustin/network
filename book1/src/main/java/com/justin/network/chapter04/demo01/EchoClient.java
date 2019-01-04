package com.justin.network.chapter04.demo01;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * 阻塞的客户端
 */
public class EchoClient {
    private SocketChannel socketChannel = null;

    public EchoClient() throws IOException {
        socketChannel = SocketChannel.open();
        InetAddress inetAddress = InetAddress.getLocalHost();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, 8000);
        socketChannel.connect(inetSocketAddress);
        System.out.println("Connected to server");
    }

    public static void main(String args[]) throws IOException {
        new EchoClient().talk();
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        return new PrintWriter(outputStream, true);
    }

    private BufferedReader getReader(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    public void talk() throws IOException {
        try {
            BufferedReader bufferedReader = getReader(socketChannel.socket());
            PrintWriter printWriter = getWriter(socketChannel.socket());
            BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));

            String msg = null;

            while((msg = localReader.readLine()) != null) {
                printWriter.println(msg);
                System.out.println(bufferedReader.readLine());

                if (msg.equals("bye")) {
                    break;
                }
            }
        }  catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (socketChannel != null) {
                    socketChannel.close();
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}