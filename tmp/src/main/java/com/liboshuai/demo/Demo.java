package com.liboshuai.demo;

import com.liboshuai.demo.function.FunctionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Demo {

    public static final Logger log = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) {
        AtomicInteger threadCount = new AtomicInteger(0);
        ExecutorService ioExecutor = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r);
            t.setName("io-executor-" + threadCount.getAndIncrement());
            return t;
        });
        log.info("组装异步任务链集合");
        List<CompletableFuture<Void>> cfList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int count = i;
            log.info("第{}次执行...", count);
            CompletableFuture<Void> cf = CompletableFuture.supplyAsync(
                    FunctionUtils.uncheckedSupplier(
                            () -> {
                                log.info("---supplyAsync[{}]---", count);
                                TimeUnit.SECONDS.sleep(1);
                                return "name" + count;
                            }
                    ),
                    ioExecutor
            ).thenApplyAsync(
                    FunctionUtils.uncheckedFunction(
                            s -> {
                                log.info("---thenApplyAsync[{}]---", count);
                                TimeUnit.SECONDS.sleep(2);
                                return "name" + "age" + count;
                            }
                    ),
                    ioExecutor
            ).thenAccept(
                    FunctionUtils.uncheckedConsumer(
                            s -> {
                                log.info("---thenAccept[{}]---", count);
                                log.info("最终的结果: {}", s);
                            }
                    )
            );
            cfList.add(cf);
        }
        log.info("异步任务链已经提交了");
        CompletableFuture<Void> allCf = CompletableFuture.allOf(cfList.toArray(new CompletableFuture[0]));
        allCf.join();
        log.info("所有异步任务都执行完毕");
        ioExecutor.shutdown();
        log.info("线程池已关闭");
    }

}

