package com.liboshuai.demo.echo.socket;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Java 原生 Socket 实现的阻塞 I/O（BIO）回声客户端
 */
@Slf4j
public class BioEchoClient {
    public static void main(String[] args) {
        String hostname = "127.0.0.1";
        int port = 8080;

        try (
                Socket echoSocket = new Socket(hostname, port);
                PrintWriter printWriter = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {
            log.info("连接到服务端，主机: {}, 端口: {}", hostname, port);
            log.info("请输入文本（输入exit退出）：");
            String userInput;
            while ((userInput = scanner.nextLine()) != null) {
                if ("exit".equalsIgnoreCase(userInput)) {
                    break;
                }
                // 发送数据到服务端
                printWriter.println(userInput);
                // 阻塞等待服务端响应
                log.info("接收到来自服务端的响应数据：{}", bufferedReader.readLine());
                log.info("请输入文本（输入exit退出）：");
            }

        } catch (IOException e) {
            log.error("连接到服务端失败，主机:{}, 端口: {}", hostname, port);
        }
    }
}
