package com.liboshuai.demo.echo.socket;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Java原生Socket实现的阻塞I/O（BIO）回声服务器
 */
@Slf4j
public class BioEchoServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        // 创建一个固定大小的线程池来处理客户端连接
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("Socket版本回声服务器已经启动，端口：{}", port);
            while (true) {
                // 1. 阻塞点一：等待客户端连接
                // serverSocket.accept() 方法会一直阻塞，直到有新的客户端连接进来。
                final Socket clientSocket = serverSocket.accept();
                log.info("接收到连接客户端的连接：{}", clientSocket.getRemoteSocketAddress());

                // 2. 为每个连接创建一个新的任务，并提交到线程池处理
                executorService.submit(new Task(clientSocket));
            }
        }
    }

    /**
     * 回显任务处理逻辑
     */
    static class Task implements Runnable {

        private final Socket clientSocket;

        Task(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {
                String line;
                // 3. 阻塞点二：等待客户端发送数据
                // reader.readLine() 方法会阻塞，直到客户端发送一行以换行符结尾的数据。
                while ((line = reader.readLine()) != null) {
                    log.info("接收到来自客户端的数据：{}", line);
                    // 将接收到的数据写回客户端
                    writer.println(line);
                }
            } catch (IOException e) {
                log.error("处理客户端连接错误", e);
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    log.error("关闭客户端连接时出现异常", e);
                }
            }
        }
    }
}
