package com.justin.network.chapter04.demo01;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 阻塞的SocketServer
 */
public class EchoServer {
    private int port = 8000;
    private ServerSocketChannel serverSocketChannel = null;
    private ExecutorService executorService;
    private static final int POOL_MULTIPLE = 4;

    public EchoServer() throws IOException {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_MULTIPLE);
        serverSocketChannel = ServerSocketChannel.open();
        // 再同一个机器上重启Server后可以立即绑定相同端口
        serverSocketChannel.socket().setReuseAddress(true);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        System.out.println("Server start");
    }

    public void service() {
        while(true) {
            SocketChannel socketChannel = null;
            try {
                socketChannel = serverSocketChannel.accept();
                executorService.execute(new Handler(socketChannel));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String args[]) throws IOException {
        new EchoServer().service();
    }

    class Handler implements Runnable {
        private SocketChannel socketChannel;

        public Handler(SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
        }

        private PrintWriter getWriter(Socket socket) throws IOException {
            OutputStream outputStream = socket.getOutputStream();
            return new PrintWriter(outputStream, true);
        }

        private BufferedReader getReader(Socket socket) throws IOException {
            InputStream inputStream = socket.getInputStream();
            return new BufferedReader(new InputStreamReader(inputStream));
        }

        public void run() {
            handle(socketChannel);
        }

        public void handle(SocketChannel socketChannel) {
            try {
                Socket socket = socketChannel.socket();
                System.out.println("Rceived client connection from :" + socket.getInetAddress() + " : " + socket.getPort());

                BufferedReader br = getReader(socket);
                PrintWriter pw = getWriter(socket);

                String msg = null;
                while((msg = br.readLine()) != null) {
                    System.out.println(msg);
                    pw.println(msg);
                    if (msg.equals("bye"))
                        break;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (socketChannel != null) socketChannel.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}