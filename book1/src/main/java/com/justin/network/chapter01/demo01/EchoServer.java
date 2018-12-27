package com.justin.network.chapter01.demo01;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class EchoServer {
    private int port = 8000;
    private ServerSocket serverSocket;

    public static void main(String args[]) throws IOException {
        new EchoServer().service();
    }

    public EchoServer() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started");
    }

    public String echo(String msg) {
        return "echo :" + msg;
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        return new PrintWriter(outputStream, true);
    }

    private BufferedReader getReader(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    public void service() {
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();

                System.out.println("Net connection accepted" +
                        socket.getInetAddress() +
                        " : " +
                        socket.getPort());
                BufferedReader bufferedReader = getReader(socket);
                PrintWriter printWriter = getWriter(socket);

                String msg = null;

                while((msg = bufferedReader.readLine()) != null) {
                    System.out.println(msg);
                    printWriter.print(echo(msg));
                    if (msg.equals("bye")) {
                        break;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}