package com.justin.network.chapter08.demo05;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class EchoClient {
    private DatagramChannel datagramChannel = null;
    private ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
    private Charset charset = Charset.forName("GBK");
    private Selector selector;

    public EchoClient() throws IOException {
        this(7000);
    }

    public EchoClient(int port) throws IOException {
        datagramChannel = DatagramChannel.open();
        InetAddress ia = InetAddress.getLocalHost();
        InetSocketAddress isa = new InetSocketAddress(ia, port);
        datagramChannel.configureBlocking(false);
        datagramChannel.socket().bind(isa);
        isa = new InetSocketAddress(ia, 8000);
        datagramChannel.connect(isa);
        selector = Selector.open();
    }

    public static void main(String args[]) throws IOException {
        int port = 7000;
        if (args.length > 0)
            port = Integer.parseInt(args[0]);

        final EchoClient client = new EchoClient(port);
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
            while ((msg = localReader.readLine()) != null) {
                synchronized (sendBuffer) {
                    sendBuffer.put(encode(msg + "\r\n"));
                }

                if (msg.equals("bye"))
                    break;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void talk() throws IOException {
        datagramChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        while (selector.select() > 0) {
            Set readKeys = selector.selectedKeys();
            Iterator it = readKeys.iterator();
            while (it.hasNext()) {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void send(SelectionKey key) throws IOException {
        DatagramChannel datagramChannel = (DatagramChannel) key.channel();
        synchronized (sendBuffer) {
            sendBuffer.flip();
            datagramChannel.write(sendBuffer);
            sendBuffer.compact();
        }
    }

    public void receive(SelectionKey key) throws IOException {
        DatagramChannel datagramChannel = (DatagramChannel) key.channel();
        datagramChannel.read(receiveBuffer);
        receiveBuffer.flip();
        String receiveData = decode(receiveBuffer);

        if (receiveData.indexOf("\n") == -1) return;

        String outputData = receiveData.substring(0, receiveData.indexOf("\n") + 1);
        System.out.print(outputData);
        if (outputData.equals("echo:bye\r\n")) {
            key.cancel();
            datagramChannel.close();
            System.out.println("close connection with server");
            selector.close();
            System.exit(0);
        }

        ByteBuffer temp = encode(outputData);
        receiveBuffer.position(temp.limit());
        receiveBuffer.compact();
    }

    public String decode(ByteBuffer buffer) {
        CharBuffer charBuffer = charset.decode(buffer);
        return charBuffer.toString();
    }

    public ByteBuffer encode(String str) {
        return charset.encode(str);
    }
}
