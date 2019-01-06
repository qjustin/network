package com.justin.network.chapter04.demo02;

import com.sun.org.apache.bcel.internal.generic.Select;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;


/**
 * 非阻塞的客户端
 * 同步，A向B发送一批数据后，必须等到B响应后才能发送下一批数据。
 * 异步，AB操作互不干扰，各自独立。
 *
 * 通信的两端并不要求采用一样的通信方式。
 *
 */
public class EchoClient {
     private SocketChannel socketChannel = null;
     private ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
     private ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
     private Charset charset = Charset.forName("GBK");
     private Selector selector;

    public EchoClient() throws IOException {
        socketChannel = SocketChannel.open();
        InetAddress inetAddress = InetAddress.getLocalHost();
        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, 8000);
        socketChannel.connect(inetSocketAddress);
        socketChannel.configureBlocking(false);
        System.out.println("Connected to server");
        selector = Selector.open();
    }

    public static void main(String args[]) throws IOException {
        final EchoClient client = new EchoClient();

        Thread receiver = new Thread() {
            public void run() {
                client.receiveFromUser();
            }
        };

        receiver.start();
        client.talk();
    }

    public void receiveFromUser() {
        try {
            BufferedReader localReader = new BufferedReader(new InputStreamReader(System.in));
            String msg = null;
            while((msg = localReader.readLine()) != null) {
                synchronized (sendBuffer) {
                    sendBuffer.put(charset.encode(msg + "\r\n"));
                }

                if (msg.equals("byte")) break;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        while (selector.select() > 0) {
            Set readyKeys = selector.selectedKeys();
            Iterator it = readyKeys.iterator();

            while(it.hasNext()) {
                SelectionKey key = null;
                try {
                    key = (SelectionKey) it.next();
                    it.remove();
                    if (key.isReadable()) {
                        receive(key);
                    }

                    if (key.isWritable()) {
                        send(key);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();

                    try {
                        if (key != null) {
                            key.cancel();
                            key.channel().close();
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void send(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel)key.channel();

        synchronized (sendBuffer) {
            sendBuffer.flip();
            socketChannel.write(sendBuffer);
            sendBuffer.compact();
        }
    }

    public void receive(SelectionKey key) throws IOException {
        SocketChannel socketChannle = (SocketChannel)key.channel();
        socketChannle.read(receiveBuffer);
        receiveBuffer.flip();
        String receiveData = charset.decode(receiveBuffer).toString();

        if (receiveData.indexOf("\n") == -1) return;

        String outputData = receiveData.substring(0, receiveData.indexOf("\n") + 1);
        if (outputData.equals("bye\r\n")) {
            key.cancel();
            socketChannle.close();
            System.out.println("close connection");
            selector.close();
            System.exit(0);
        }

        ByteBuffer temp = charset.encode(outputData);
        receiveBuffer.position(temp.limit());
        receiveBuffer.compact();
    }
}