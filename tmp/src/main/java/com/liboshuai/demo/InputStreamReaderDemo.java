package com.liboshuai.demo;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class InputStreamReaderDemo {
    public static void main(String[] args) {
        try (// 1. FileInputStream 是被适配者，提供字节流
             InputStream fileInputStream = Files.newInputStream(Paths.get("my-file.txt"));

             // 2. InputStreamReader 是适配器，将字节流转换为字符流
             //    客户端希望使用的是 Reader 接口
             Reader reader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8)
        ) {

            // 3. 客户端现在可以通过 Reader 接口愉快地读取字符了
            int charData;
            while ((charData = reader.read()) != -1) {
                System.out.print((char) charData);
            }

        } catch (IOException e) {
            log.error("", e);
        }
    }
}
