package com.justin.network.chapter01.demo01;

import java.io.*;
import java.net.Socket;

/**
 * PrintWriter ： 向标准输出设备输出流，用来输出各种类型的数据。PrintWriter，有一系列的print方法和println方法，有缓冲区功能，
 * 需要及时关闭或者flush,可以写入基本类型（转换为字符串）、字符串和对象（使用toString方法）
 * PrintWriter 简单的理解为向OutputStream中写入数据的工具类。
 *
 * InputStream：从外部进入内部的流（数据） 从InputStream中读取数据
 * OutputStream：从内部发到外部的流（数据）往OutputStream中写入数据
 *
 */
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
                // 将msg发送到服务端
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