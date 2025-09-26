package com.liboshuai.demo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.*;

// ============================================================================
// 1. 核心接口与抽象类
// ============================================================================

/**
 * RpcGateway: 一个用于标识 RPC 网关的标记接口。
 */
interface RpcGateway {
}

/**
 * RpcService: 定义了 RPC 服务的核心功能。
 */
interface RpcService {
    /**
     * 启动一个 RPC 端点服务。
     * @param endpoint 要启动的端点。
     */
    void startServer(RpcEndpoint endpoint);

    /**
     * 停止一个指定地址的 RPC 端点服务。
     * @param address 要停止的端点的地址。
     */
    void stopServer(String address);

    /**
     * 连接到一个远程的 RPC 端点，并返回一个该端点的网关代理。
     * @param address 目标端点的地址。
     * @param clazz 目标网关的接口类型。
     * @param <C> 网关类型。
     * @return 一个包含网关代理的 CompletableFuture。
     */
    <C extends RpcGateway> CompletableFuture<C> connect(String address, Class<C> clazz);

    /**
     * 停止整个 RPC 服务，并清理所有资源。
     */
    void stopService();
}

/**
 * RpcEndpoint: RPC 端点的抽象基类，封装了所有端点的通用逻辑。
 */
abstract class RpcEndpoint {
    private final RpcService rpcService;
    private final String address;
    private volatile ScheduledExecutorService mainThreadExecutor;

    public RpcEndpoint(RpcService rpcService, String address) {
        this.rpcService = rpcService;
        this.address = address;
    }

    public RpcService getRpcService() { return rpcService; }
    public String getAddress() { return address; }
    public ScheduledExecutorService getMainThreadExecutor() { return mainThreadExecutor; }
    void setMainThreadExecutor(ScheduledExecutorService mainThreadExecutor) { this.mainThreadExecutor = mainThreadExecutor; }

    /**
     * 在端点的单线程执行器中异步执行一个任务。
     * @param runnable 要执行的任务。
     */
    protected void runAsync(Runnable runnable) {
        mainThreadExecutor.execute(runnable);
    }

    /**
     * 在端点的单线程执行器中异步调用一个函数并返回一个 Future。
     * @param callable 要调用的函数。
     * @return 一个包含调用结果的 CompletableFuture。
     */
    protected <V> CompletableFuture<V> callAsync(Callable<V> callable) {
        CompletableFuture<V> future = new CompletableFuture<>();
        mainThreadExecutor.execute(() -> {
            try {
                future.complete(callable.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    /**
     * 端点启动时的回调方法。
     */
    public void onStart() throws Exception {}

    /**
     * 端点停止时的回调方法。
     */
    public void onStop() throws Exception {}
}

// ============================================================================
// 2. 核心实现类
// ============================================================================

/**
 * SimpleRpcService: RpcService 接口的一个简单实现。
 */
class SimpleRpcService implements RpcService {
    private final Map<String, RpcEndpoint> endpoints = new ConcurrentHashMap<>();
    private final Map<String, ScheduledExecutorService> executors = new ConcurrentHashMap<>();

    @Override
    public void startServer(RpcEndpoint endpoint) {
        if (endpoints.containsKey(endpoint.getAddress())) {
            throw new IllegalArgumentException("地址为 " + endpoint.getAddress() + " 的 RPC 端点已经被注册。");
        }
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        endpoints.put(endpoint.getAddress(), endpoint);
        executors.put(endpoint.getAddress(), executor);
        endpoint.setMainThreadExecutor(executor);
        executor.execute(() -> {
            try {
                endpoint.onStart();
                System.out.println("已启动 RpcEndpoint: " + endpoint.getAddress());
            } catch (Exception e) {
                System.err.println("启动 RpcEndpoint " + endpoint.getAddress() + " 失败");
                e.printStackTrace();
            }
        });
    }

    @Override
    public void stopServer(String address) {
        RpcEndpoint endpoint = endpoints.remove(address);
        if (endpoint != null) {
            ScheduledExecutorService executor = executors.remove(address);
            if (executor != null) {
                executor.execute(() -> {
                    try {
                        endpoint.onStop();
                        System.out.println("已停止 RpcEndpoint: " + address);
                    } catch (Exception e) {
                        System.err.println("停止 RpcEndpoint " + address + " 失败");
                        e.printStackTrace();
                    } finally {
                        executor.shutdown();
                    }
                });
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C extends RpcGateway> CompletableFuture<C> connect(String address, Class<C> clazz) {
        RpcEndpoint endpoint = endpoints.get(address);
        if (endpoint == null) {
            return CompletableFuture.supplyAsync(() -> {
                throw new IllegalStateException("找不到地址为 " + address + " 的 RPC 端点。");
            });
        }
        RpcInvocationHandler handler = new RpcInvocationHandler(endpoint);

        // 使用 Java 动态代理为网关接口创建一个代理实例。
        // 所有对代理实例的方法调用都会被转发到 RpcInvocationHandler 的 invoke 方法。
        C proxy = (C) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class<?>[] { clazz },
                handler
        );
        return CompletableFuture.completedFuture(proxy);
    }

    @Override
    public void stopService() {
        System.out.println("正在停止 RpcService...");
        // 遍历键集合的副本，以避免在迭代过程中修改集合时出现 ConcurrentModificationException。
        for (String address : new java.util.HashSet<>(endpoints.keySet())) {
            stopServer(address);
        }
        endpoints.clear();
        executors.clear();
        System.out.println("RpcService 已停止。");
    }
}

/**
 * RpcInvocationHandler: 用于 RPC 调用的动态代理处理器。
 * 它拦截对网关接口的方法调用，并将其转发到目标 RpcEndpoint 的主线程中执行。
 */
class RpcInvocationHandler implements InvocationHandler {
    private final RpcEndpoint targetEndpoint;

    public RpcInvocationHandler(RpcEndpoint targetEndpoint) {
        this.targetEndpoint = targetEndpoint;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 处理来自 Object 类的方法，例如 toString(), hashCode() 等，直接在本对象上调用。
        if (method.getDeclaringClass().equals(Object.class)) {
            return method.invoke(this, args);
        }

        // 核心逻辑: 异步调用并处理嵌套的 CompletableFuture。

        // 1. 异步执行目标端点的方法调用。这个调用可能会直接返回一个值，或者返回一个 CompletableFuture。
        //    无论如何，执行结果都会被包装在一个外层的 CompletableFuture (outerFuture) 中。
        CompletableFuture<Object> outerFuture = targetEndpoint.callAsync(() -> method.invoke(targetEndpoint, args));

        // 2. 使用 thenCompose 来“展开”或“扁平化”结果。
        //    - 如果内部返回的是一个普通值（例如 String），thenCompose 会将其包装成一个已完成的 Future。
        //    - 如果内部返回的是另一个 CompletableFuture，thenCompose 会等待这个内部 Future 完成，并采用其结果。
        //    这种方式可以优雅地处理端点方法同步返回和异步返回两种情况。
        return outerFuture.thenCompose(result -> {
            if (result instanceof CompletableFuture) {
                return (CompletableFuture) result;
            } else {
                return CompletableFuture.completedFuture(result);
            }
        });
    }
}

// ============================================================================
// 3. 示例 Demo 类
// ============================================================================

/**
 * JobManagerGateway: JobManager 的 RPC 网关接口。
 */
interface JobManagerGateway extends RpcGateway {
    CompletableFuture<String> registerTaskManager(String taskManagerAddress);
}

/**
 * TaskManagerGateway: TaskManager 的 RPC 网关接口。
 */
interface TaskManagerGateway extends RpcGateway {
    CompletableFuture<String> notifyOfJobSuccess(String jobId);
}

/**
 * JobManagerEndpoint: JobManager 端点的具体实现。
 */
class JobManagerEndpoint extends RpcEndpoint implements JobManagerGateway {
    private final Map<String, TaskManagerGateway> registeredTaskManagers = new ConcurrentHashMap<>();

    public JobManagerEndpoint(RpcService rpcService, String address) {
        super(rpcService, address);
    }

    @Override
    public void onStart() throws Exception {
        System.out.println("JobManagerEndpoint 正在启动。");
    }

    @Override
    public CompletableFuture<String> registerTaskManager(String taskManagerAddress) {
        // 使用 callAsync 确保此逻辑在 JobManager 的主线程中执行。
        return callAsync(() -> {
            System.out.println("JobManager 接到来自 " + taskManagerAddress + " 的注册请求。");

            // 连接到远程的 TaskManager 端点
            CompletableFuture<TaskManagerGateway> tmGatewayFuture = getRpcService().connect(taskManagerAddress, TaskManagerGateway.class);

            // 连接成功后，在 JobManager 主线程中执行后续操作。
            tmGatewayFuture.thenAccept(tmGateway -> {
                runAsync(() -> {
                    registeredTaskManagers.put(taskManagerAddress, tmGateway);
                    System.out.println("TaskManager " + taskManagerAddress + " 注册成功。总数: " + registeredTaskManagers.size());

                    // 调用 TaskManager 的方法
                    tmGateway.notifyOfJobSuccess("job-001").thenAccept(ack -> {
                        System.out.println("JobManager 收到来自 TM 的回执: " + ack);
                    });
                });
            });
            return "为 " + taskManagerAddress + " 注册成功。";
        });
    }

    @Override
    public void onStop() throws Exception {
        System.out.println("JobManagerEndpoint 正在停止。");
        registeredTaskManagers.clear();
    }
}

/**
 * TaskManagerEndpoint: TaskManager 端点的具体实现。
 */
class TaskManagerEndpoint extends RpcEndpoint implements TaskManagerGateway {
    public TaskManagerEndpoint(RpcService rpcService, String address) {
        super(rpcService, address);
    }

    @Override
    public void onStart() throws Exception {
        System.out.println("TaskManagerEndpoint 正在启动。");
    }

    @Override
    public CompletableFuture<String> notifyOfJobSuccess(String jobId) {
        // 使用 callAsync 确保此逻辑在 TaskManager 的主线程中执行。
        return callAsync(() -> {
            System.out.println("TaskManager 收到作业 " + jobId + " 成功的通知。");
            return "确认作业成功: " + jobId;
        });
    }

    @Override
    public void onStop() throws Exception {
        System.out.println("TaskManagerEndpoint 正在停止。");
    }
}

/**
 * FlinkRpcDemo: 示例程序的主入口。
 */
public class FlinkRpcDemo {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        RpcService rpcService = new SimpleRpcService();

        // 启动 JobManager
        String jmAddress = "jobmanager";
        JobManagerEndpoint jmEndpoint = new JobManagerEndpoint(rpcService, jmAddress);
        rpcService.startServer(jmEndpoint);

        // 启动 TaskManager
        String tmAddress = "taskmanager";
        TaskManagerEndpoint tmEndpoint = new TaskManagerEndpoint(rpcService, tmAddress);
        rpcService.startServer(tmEndpoint);

        // TaskManager 连接到 JobManager
        CompletableFuture<JobManagerGateway> jmGatewayFuture = rpcService.connect(jmAddress, JobManagerGateway.class);
        JobManagerGateway jmGateway = jmGatewayFuture.get(); // 阻塞等待连接成功

        System.out.println("TaskManager 准备向 JobManager 注册...");
        CompletableFuture<String> registrationFuture = jmGateway.registerTaskManager(tmAddress);

        // 处理注册结果
        registrationFuture.thenAccept(response -> {
            System.out.println("TaskManager 收到注册响应: " + response);
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });

        // 等待所有异步操作完成，以便观察日志输出
        Thread.sleep(2000);

        // 停止服务
        rpcService.stopService();
    }
}