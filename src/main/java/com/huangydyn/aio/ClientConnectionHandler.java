package com.huangydyn.aio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;

/**
 * 客户端连接处理器
 * 负责处理新的客户端连接，包括发送欢迎消息、读取客户端数据等操作
 */
public class ClientConnectionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {
    
    private final AsynchronousServerSocketChannel serverSocketChannel;
    
    /**
     * 构造方法
     * @param serverSocketChannel 服务器通道
     */
    public ClientConnectionHandler(AsynchronousServerSocketChannel serverSocketChannel) {
        this.serverSocketChannel = serverSocketChannel;
    }
    
    @Override
    public void completed(AsynchronousSocketChannel clientChannel, Object attachment) {
        // 继续监听下一个连接请求
        serverSocketChannel.accept(attachment, this);
        
        // 处理当前客户端连接
        handleClientConnection(clientChannel);
    }
    
    @Override
    public void failed(Throwable exc, Object attachment) {
        System.err.println("[错误] 接受连接失败: " + exc.getMessage());
        // 只在开发环境打印完整堆栈
        if (isDevelopmentEnvironment()) {
            exc.printStackTrace();
        }
    }
    
    /**
     * 处理客户端连接
     * @param clientChannel 客户端通道
     */
    private void handleClientConnection(AsynchronousSocketChannel clientChannel) {
        try {
            System.out.println("接受了一个连接: " + clientChannel.getRemoteAddress().toString());
            
            // 发送欢迎消息
            sendWelcomeMessage(clientChannel);
            
            // 读取客户端数据
            receiveClientData(clientChannel);
            
        } catch (IOException e) {
            handleClientError("获取客户端地址失败", e);
        } finally {
            // 关闭客户端通道
            closeClientChannel(clientChannel);
        }
    }
    
    /**
     * 关闭客户端通道
     * @param clientChannel 客户端通道
     */
    private void closeClientChannel(AsynchronousSocketChannel clientChannel) {
        try {
            if (clientChannel != null && clientChannel.isOpen()) {
                clientChannel.close();
                System.out.println("客户端通道已关闭");
            }
        } catch (IOException e) {
            System.err.println("关闭客户端通道失败: " + e.getMessage());
            if (isDevelopmentEnvironment()) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 发送欢迎消息
     * @param clientChannel 客户端通道
     */
    private void sendWelcomeMessage(AsynchronousSocketChannel clientChannel) {
        try {
            ByteBuffer welcomeBuffer = ByteBuffer.wrap("From Server:Hello i am server".getBytes());
            clientChannel.write(welcomeBuffer).get();
            System.out.println("已发送欢迎消息到客户端");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handleClientError("发送消息被中断", e);
        } catch (ExecutionException e) {
            handleClientError("发送欢迎消息失败", e);
        }
    }
    
    /**
     * 接收客户端数据
     * @param clientChannel 客户端通道
     */
    private void receiveClientData(AsynchronousSocketChannel clientChannel) {
        try {
            ByteBuffer readBuffer = ByteBuffer.allocate(128);
            clientChannel.read(readBuffer).get();
            
            // 重置缓冲区位置，准备读取
            readBuffer.flip();
            if (readBuffer.hasRemaining()) {
                byte[] data = new byte[readBuffer.limit()];
                readBuffer.get(data);
                
                String clientMessage = new String(data).trim();
                System.out.println("接收客户端数据: " + clientMessage);
            } else {
                System.out.println("客户端未发送数据");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            handleClientError("读取数据被中断", e);
        } catch (ExecutionException e) {
            handleClientError("读取客户端数据失败", e);
        }
    }
    
    /**
     * 处理客户端错误
     * @param message 错误消息
     * @param e 异常
     */
    private void handleClientError(String message, Exception e) {
        System.err.println("[错误] " + message + ": " + e.getMessage());
        // 只在开发环境打印完整堆栈
        if (isDevelopmentEnvironment()) {
            e.printStackTrace();
        }
    }
    
    /**
     * 判断是否为开发环境
     * @return 是否为开发环境
     */
    private boolean isDevelopmentEnvironment() {
        // 简单判断是否为开发环境
        String env = System.getProperty("env", "dev");
        return "dev".equals(env);
    }
}
