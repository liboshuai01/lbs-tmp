package com.liboshuai.demo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class MyServiceSimulator {

    private static final Logger log = LoggerFactory.getLogger(MyServiceSimulator.class);

    private static final String SERVER_PORT = "server.port";

    private boolean initialized = false;

    private final Properties config = new Properties();

    private final String serviceId;

    public MyServiceSimulator(String serviceId) {
        this.serviceId = serviceId;
    }


    public void loadConfig(String configPath) throws InvalidConfigurationException {
        if (configPath == null || configPath.isEmpty()) {
            throw new InvalidConfigurationException("配置文件路径不能为空。");
        }
        try (FileInputStream fis = new FileInputStream(configPath)) {
            config.load(fis);
            String serverPort = config.getProperty(SERVER_PORT);
            if (serverPort == null) {
                throw new InvalidConfigurationException("缺少必填配置项：'" + SERVER_PORT + "'");
            }
            int ignore = Integer.parseInt(serverPort);
            this.initialized = true;
        } catch (FileNotFoundException e) {
            throw new InvalidConfigurationException("配置文件未在以下路径找到：" + configPath, e);
        } catch (NumberFormatException e) {
            throw new InvalidConfigurationException("端口号无效：" + config.getProperty(SERVER_PORT), e);
        } catch (IOException e) {
            throw new InvalidConfigurationException("无法读取配置文件：" + configPath, e);
        }
    }

    public void run() {
        if (!initialized) {
            throw new InvalidOperationException("服务未初始化，无法运行()，您是否忘记调用 loadConfig()？");
        }
        String serverPort = config.getProperty(SERVER_PORT);
        log.info("服务运行端口：{}", serverPort);

        if ("9999".equals(serverPort)) {
            throw new ServiceExecutionException(this.serviceId, "模拟严重故障：端口 9999 已保留用于故障。", null);
        }
        log.info("服务 [{}] 运行成功。", this.serviceId);
    }

    public void simulateSuccessfulLoad(String port) {
        this.config.setProperty("server.port", port);
        this.initialized = true;
        log.info("服务[{}]已成功模拟配置，端口为{}。", this.serviceId, port);
    }

    public static void main(String[] args) {
        MyServiceSimulator service = new MyServiceSimulator("service-001");

        log.info("--- 尝试加载配置（场景 1：捕获受检异常）---");
        try {
            service.loadConfig("nonexistent-file.properties");
            service.run();
        } catch (InvalidConfigurationException e) {
            log.error("加载配置失败：", e);
        }

        log.info("--- 尝试运行服务（场景 2：捕获非受检异常）---");
        try {
            service.run();
        } catch (InvalidOperationException e) {
            log.error("操作失败：", e);
        } catch (MyProjectRuntimeException e) {
            log.error("项目通用运行时异常：", e);
        }

        log.info("--- 尝试运行服务（场景 3：捕获带ID的新异常）---");
        MyServiceSimulator service2 = new MyServiceSimulator("service-002");
        try {
            service2.simulateSuccessfulLoad("9999");
            service2.run();
        } catch (ServiceExecutionException e) {
            log.error("服务执行失败（Service ID: {}）: ", e.getServiceId(), e);
        }
    }
}
