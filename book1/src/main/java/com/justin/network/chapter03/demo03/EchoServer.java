package com.justin.network.chapter03.demo03;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public class EchoServer {
    private int port = 8000;
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private final int POOL_SIZE = 2;

    private int portForShutdown = 8001;
    private ServerSocket serverSocketForShutdown;
    private boolean isShutdown = false;

    private Thread shutdownThread = new Thread() {
        public void start() {
            this.setDaemon(true);
            super.start();
        }

        public void run() {
            while(!isShutdown) {
                Socket socketForShutdown = null;

                try {
                    socketForShutdown = serverSocketForShutdown.accept();
                    BufferedReader br = new BufferedReader(new InputStreamReader(socketForShutdown.getInputStream()));
                    String command = br.readLine();

                    if (command.equals("shutdown")) {
                        long beginTime = System.currentTimeMillis();
                        socketForShutdown.getOutputStream().write("Server is closing".getBytes());
                        isShutdown = true;
                        executorService.shutdown();

                        while(!executorService.isTerminated())
                            executorService.awaitTermination(30, TimeUnit.SECONDS);

                        serverSocket.close();;
                        long endTime = System.currentTimeMillis();
                        socketForShutdown.getOutputStream().write("server is closed".getBytes());
                        socketForShutdown.close();
                    } else {
                        socketForShutdown.getOutputStream().write("Command error".getBytes());
                        socketForShutdown.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    public EchoServer() throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(60000);
        serverSocketForShutdown = new ServerSocket(portForShutdown);

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
        shutdownThread.start();

        System.out.println("Server started");

    }

    public void service() {
        while (!isShutdown) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                socket.setSoTimeout(60000);
                executorService.execute(new Handler(socket));
            } catch (SocketTimeoutException ex) {
                // nothing to do
            } catch (RejectedExecutionException ex) {
                try {
                    if (socket != null) socket.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                return;
            } catch (SocketException ex)  {
                if (ex.getMessage().indexOf("socket closed") != -1)
                    return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) throws IOException {
        new EchoServer().service();
    }

    class Handler implements Runnable {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
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
            try {
                System.out.println("New connection accepted " + socket.getPort() + ":" + socket.getInetAddress());

                BufferedReader br = getReader(socket);
                PrintWriter pw = getWriter(socket);

                String msg = null;
                while ((msg = br.readLine()) != null) {
                    System.out.println(msg);
                    pw.println(msg);
                    if ("byt".equals(msg))
                        break;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
