package com.huangydyn.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

class SocketServerThread implements Runnable {
    private Socket socket;

    public SocketServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        InputStream in = null;
        OutputStream out = null;
        try {
            // 线程处理
            in = socket.getInputStream();
            out = socket.getOutputStream();
            Integer sourcePort = socket.getPort();
            int maxLen = 1024;
            byte[] contextBytes = new byte[maxLen];

            // 线程阻塞在read直到操作系统有数据准备好
            int realLen = in.read(contextBytes, 0, maxLen);
            String message = new String(contextBytes, 0, realLen);

            Thread.sleep(100);
            //下面打印信息
            System.out.println("服务器收到来自于端口：" + sourcePort + ", 处理线程" + Thread.currentThread().getId());
            //下面开始发送信息
            out.write("回发响应信息！".getBytes());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            //试图关闭
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (this.socket != null) {
                    this.socket.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}