package com.justin.network.chapter08.demo02;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DatagramTester {
    private int port = 8000;
    private DatagramSocket sendSocket;
    private DatagramSocket receiveSocket;
    private static final int MAX_LENGTH = 3584;

  public DatagramTester() throws IOException {
        sendSocket = new DatagramSocket();
        receiveSocket = new DatagramSocket(port);
        receiver.start();
        sender.start();
    }

    public static byte[] longToByte(long[] data) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        for (int i = 0; i < data.length; i++) {
            dataOutputStream.writeLong(data[i]);
        }

        dataOutputStream.close();
        return outputStream.toByteArray();
    }

    public static long[] byteToLong(byte[] data) throws IOException {
        long[] result = new long[data.length / 8];
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        for (int i = 0; i < data.length / 8; i++) {
            result[i] = dataInputStream.readLong();
        }

        return result;
    }

    public void send(byte[] bigData) throws IOException {
        DatagramPacket packet = new DatagramPacket(bigData, 0, 512, InetAddress.getByName("localhost"), port);
        // 表示已经发送的字节数
        int bytesSent = 0;
        // 表示发送的次数
        int count = 0;
        while (bytesSent < bigData.length) {
            sendSocket.send(packet);
            System.out.println("SendSocket > 第" + (++count) + " 次发送了" + packet.getLength() + "个字节");
            // getLength()方法返回实际发送的字节数
            bytesSent += packet.getLength();
            // 计算剩余的未发送的字节数
            int remain = bigData.length - bytesSent;
            // 计算下次发送的数据的长度
            int length = (remain > 512) ? 512 : remain;
            // 改变DatagramPacket的offset和length属性
            packet.setData(bigData, bytesSent, length);
        }
    }

    public byte[] receive() throws IOException {
        byte[] bigData = new byte[MAX_LENGTH];
        DatagramPacket packet = new DatagramPacket(bigData, 0, MAX_LENGTH);
        int bytesReceived = 0;
        int count = 0;
        long beginTime = System.currentTimeMillis();

        while (bytesReceived < bigData.length && (System.currentTimeMillis() - beginTime < 60000 * 5)) {
            receiveSocket.receive(packet);
            System.out.println("SendSocket > 第" + (++count) + " 次发送了" + packet.getLength() + "个字节");
            bytesReceived += packet.getLength();
            packet.setData(bigData, bytesReceived, MAX_LENGTH - bytesReceived);
        }

        return packet.getData();
    }

    public Thread sender = new Thread() {
        public void run() {
            long[] longArray = new long[MAX_LENGTH / 8];
            for (int i = 0; i < longArray.length; i++) {
                longArray[i] = i + 1;
                try {
                    send(longToByte(longArray));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    };

    public Thread receiver = new Thread() {
        public void run() {
            try {
                long[] longArray = byteToLong(receive());

                for (int i = 0; i < longArray.length; i++) {
                    if (i % 100 == 0) System.out.println();
                    System.out.print(longArray[i] + " ");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    };

    public static void main(String args[]) throws IOException {
        DatagramTester tester = new DatagramTester();
    }
}
