package com.justin.network.chapter02.demo01;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Sender {
    public String host = "localhost";
    private int port = 8000;
    private Socket socket;
    private static int stopWay = 1;
    private final int NATURAL_STOP = 1;
    private final int SUDDEN_STOP = 2;
    private final int SOCKET_STOP = 3;
    private final int OUTPUT_STOP = 4;

    public Sender() throws IOException {
        socket = new Socket(host, port);
    }

    public static void main(String args[]) throws Exception {
        if (args.length > 0) stopWay = Integer.parseInt(args[0]);
        new Sender().send();
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        return new PrintWriter(outputStream, true);
    }

    public void send() throws Exception {
        PrintWriter pw = getWriter(socket);

        for (int i = 0; i < 20; i++) {
            String msg = "hello - " + i;
            pw.println(msg);
            System.out.println("send:" + msg);
            Thread.sleep(500);

            if (i == 2) {
                if (stopWay == SUDDEN_STOP) {
                    System.out.println("SUDDEN STOP");
                    System.exit(0);
                } else if (stopWay == SOCKET_STOP) {
                    System.out.println("SOCKET STOP");
                    socket.close();
                } else if (stopWay == OUTPUT_STOP) {
                    System.out.println("OUTPUT STOP");
                    socket.shutdownOutput();
                    break;
                }
            }
        }

        if (stopWay == NATURAL_STOP) {
            socket.close();
        }
    }
}
