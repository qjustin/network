package com.justin.network.chapter04.demo03;

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

/**
 * 混合使用阻塞模式与非阻塞的EchoServer
 */
public class EchoServer {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel = null;
    private int port = 8000;
    private Charset charset = Charset.forName("GBK");

    // SelectableChannel.register() 与 Selector.select() 方法都会操作 all-keys 集合。 未避免资源竞争，需要加锁
    private Object lockObj = new Object();

    public EchoServer() throws IOException {
        // 创建一个selector
        selector = Selector.open();

        // 创建一个ServerSocketChannel对象
        serverSocketChannel = ServerSocketChannel.open();

        // 再同一个机器上重启Server后可以立即绑定相同端口
        serverSocketChannel.socket().setReuseAddress(true);

        // 绑定端口
        serverSocketChannel.socket().bind(new InetSocketAddress(port));

        System.out.println("Server start");
    }

    public void accept() {
        for (; ; ) {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                synchronized (lockObj) {
                    selector.wakeup();
                    socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, buffer);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void service() throws IOException {
        for (; ; ) {
            synchronized (lockObj) {
                int n = selector.select();
                if (n == 0) continue;

                Set readKeys = selector.selectedKeys();
                Iterator it = readKeys.iterator();

                //循环处理每个SelectionKey
                while (it.hasNext()) {
                    SelectionKey key = null;

                    try {
                        key = (SelectionKey) it.next();
                        it.remove();
                        // 处理接收连接就绪事件由一个独立的线程处理这里不再处理

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

                            while (outputBuffer.hasRemaining()) {
                                socketChannel.write(outputBuffer);
                                ByteBuffer temp = charset.encode(outputData);
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
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static void main(String args[]) throws IOException {
        final EchoServer server = new EchoServer();
        Thread acceptThread = new Thread() {
            public void run() {
                server.accept();
            }
        };

        acceptThread.start();
        server.service();
    }
}