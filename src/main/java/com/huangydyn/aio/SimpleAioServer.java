package com.huangydyn.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * 简单的AIO服务器实现
 * 使用异步非阻塞IO模型
 */
public class SimpleAioServer {
    
    private AsynchronousServerSocketChannel serverSocketChannel;
    private static final int PORT = 8002;
    
    /**
     * 启动服务器
     */
    public void start() {
        try {
            initializeServer();
            setupHandler();
            keepServerRunning();
        } catch (IOException e) {
            handleServerError("服务器启动失败", e);
            // 启动失败后退出程序
            System.exit(1);
        } catch (Exception e) {
            handleServerError("服务器发生未知错误", e);
            // 发生未知错误后退出程序
            System.exit(1);
        }
    }
    
    /**
     * 初始化服务器
     * @throws IOException IO异常
     */
    private void initializeServer() throws IOException {
        System.out.println("[启动] 服务端启动开始");
        serverSocketChannel = AsynchronousServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(PORT));
        System.out.println("[启动] 服务端绑定端口：" + PORT);
    }
    
    /**
     * 设置连接处理器
     */
    private void setupHandler() {
        // 使用外部的ClientConnectionHandler类
        ClientConnectionHandler handler = new ClientConnectionHandler(serverSocketChannel);
        serverSocketChannel.accept(null, handler);
        System.out.println("[启动] 服务端启动完成");
    }
    
    /**
     * 处理服务器错误
     * @param message 错误消息
     * @param e 异常
     */
    private void handleServerError(String message, Exception e) {
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
    
    /**
     * 保持主线程运行
     */
    private void keepServerRunning() {
        // 使用CountDownLatch来保持主线程运行，比sleep更优雅
        try {
            System.out.println("[运行] 服务器已启动并运行中...");
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[错误] 服务器线程被中断：" + e.getMessage());
        }
    }

    /**
     * 关闭服务器资源
     */
    public void shutdown() {
        try {
            if (serverSocketChannel != null && serverSocketChannel.isOpen()) {
                System.out.println("[关闭] 正在关闭服务器通道...");
                serverSocketChannel.close();
                System.out.println("[关闭] 服务器通道已关闭");
            }
        } catch (IOException e) {
            System.err.println("[错误] 关闭服务器通道失败: " + e.getMessage());
            if (isDevelopmentEnvironment()) {
                e.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args) {
        SimpleAioServer server = new SimpleAioServer();
        
        // 注册关闭钩子，确保在JVM退出时关闭资源
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[关闭] 收到关闭信号，正在清理资源...");
            server.shutdown();
            System.out.println("[关闭] 资源清理完成");
        }));
        
        server.start();
    }
}
