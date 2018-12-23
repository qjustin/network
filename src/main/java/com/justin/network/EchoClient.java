package com.justin.network;

import java.io.*;
import java.net.Socket;

public class EchoClient {
    private String host = "localhost";
    private int port = 8000;
    private Socket socket;

    public EchoClient() throws IOException {
        socket = new Socket(host, port);
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
            BufferedReader bufferedReader = getReader(socket);
            PrintWriter printWriter = getWriter(socket);
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
                if (socket != null) {
                    socket.close();
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
