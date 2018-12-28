package com.justin.network.chapter02.demo01;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Receiver {
    private int port = 8000;
    private ServerSocket serverSocket;
    private static int stopWay = 1;
    private final int NATURAL_STOP = 1;
    private final int SUDDEN_STOP = 2;
    private final int SOCKET_STOP = 3;
    private final int INPUT_STOP = 4;
    private final int SERVERSOCKET_STOP = 5;

    public Receiver() throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public static void main(String args[]) throws Exception {
        if (args.length > 0) stopWay = Integer.parseInt(args[0]);
        new Receiver().receive();
    }

    private BufferedReader getReader(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        return new BufferedReader(new InputStreamReader(inputStream));
    }

    public void receive() throws Exception {
        Socket socket = serverSocket.accept();
        BufferedReader br = getReader(socket);

        for (int i = 0; i < 20; i++) {
            String msg = br.readLine();
            System.out.println("receive:" + msg);
            Thread.sleep(1000);

            if (i == 2) {
                if (stopWay == SUDDEN_STOP) {
                    System.out.println("SUDDEN STOP");
                    System.exit(0);
                } else if (stopWay == SOCKET_STOP) {
                    System.out.println("SOCKET STOP");
                    socket.close();
                } else if (stopWay == INPUT_STOP) {
                    System.out.println("INPUT STOP");
                    socket.shutdownInput();
                    break;
                }else if (stopWay == SERVERSOCKET_STOP) {
                    System.out.println("SERVERSOCKET STOP");
                    socket.close();
                    break;
                }
            }
        }

        if (stopWay == NATURAL_STOP) {
            socket.close();
            serverSocket.close();
        }
    }
}
