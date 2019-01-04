package com.justin.network.chapter04.demo02;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 非阻塞的EchoServer
 * 一个主线程，就能同时处理3个事件。
 * 1. 接收客户端的连接
 * 2. 接收客户端发送的数据
 * 3. 向客户发回响应数据
 */
public class EchoServer {
    private int port = 8000;
    private ServerSocketChannel serverSocketChannel = null;
    private Selector selector;
    private static final int POOL_MULTIPLE = 4;

    public EchoServer() throws IOException {
        // 创建一个selector
        selector = Selector.open();

        // 创建一个ServerSocketChannel对象
        serverSocketChannel = ServerSocketChannel.open();

        // 使serverSocketChannel再非阻塞模式下工作
        serverSocketChannel.configureBlocking(false);

        // 再同一个机器上重启Server后可以立即绑定相同端口
        serverSocketChannel.socket().setReuseAddress(true);

        // 绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        System.out.println("Server start");
    }

    public void service() throws IOException {
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // 循环获得Selector的selected-keys集合
        while (selector.select() > 0) {
            Set readKeys = selector.selectedKeys();
            Iterator it = readKeys.iterator();

            //循环处理每个SelectionKey
            while (it.hasNext()) {
                SelectionKey key = null;

                try {
                    key = (SelectionKey) it.next();
                    it.remove();
                    // 处理接收连接就绪事件
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        System.out.println(socketChannel.socket().getInetAddress() + ":" + socketChannel.socket().getPort());
                        socketChannel.configureBlocking(false);
                        // 用于存放用户发送过来的数据
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, buffer);
                    }

                    // 处理读就绪事件
                    if (key.isReadable()) {
                        // 获取与SelectionKey关联的附件
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        // 获取与SelectionKey关联的SocketChannel
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        // 创建一个ByteBuffer，用于存放读到的数据
                        ByteBuffer readBuff = ByteBuffer.allocate(32);
                        socketChannel.read(readBuff);
                        readBuff.flip();
                        // 把Buffer的极限设置为容量
                        buffer.limit(buffer.capacity());
                        // 把 readBuff中的内容拷贝到buffer中
                        // 假定 buffer的容量足够大，不会出现缓冲区溢出异常
                        buffer.put(readBuff);
                    }

                    // 处理写就绪事件
                    if (key.isWritable()) {
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        SocketChannel socketChannel = (SocketChannel) key.channel();

                        buffer.flip();
                        Charset charset = Charset.forName("GBK");
                        String data = charset.decode(buffer).toString();

                        if (data.indexOf("\r\n") == -1) return;

                        String outputData = data.substring(0, data.indexOf("\n") + 1);
                        System.out.println(outputData);
                        ByteBuffer outputBuffer = charset.encode("echo:" + outputData);

                        while(outputBuffer.hasRemaining()) {
                            socketChannel.write(outputBuffer);
                            ByteBuffer temp =charset.encode(outputData);
                            buffer.position(temp.limit());
                            buffer.compact();

                            if (outputData.equals("byte\r\n")) {
                                key.cancel();
                                socketChannel.close();
                                System.out.println("Closed client connection");
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();

                    try {
                        if (key != null) {
                            key.cancel();
                            key.channel().close();
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static void main(String args[]) throws IOException {
        new EchoServer().service();
    }
}