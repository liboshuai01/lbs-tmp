package com.liboshuai.demo.juc.chm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// 导入 SLF4J
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模拟 Flink JobManager 中用于跟踪 Task 状态的组件。
 */
public class TaskStateTracker {

    // 添加 SLF4J Logger
    private static final Logger log = LoggerFactory.getLogger(TaskStateTracker.class);

    /**
     * Flink 中的执行状态
     */
    public enum ExecutionState {
        CREATED,
        DEPLOYING,
        RUNNING,
        FINISHED,
        FAILED,
        CANCELING,
        CANCELED
    }

    /**
     * 存储所有 Task 的当前状态。
     * Key: Task ID (例如 "task-subtask-01")
     * Value: 该 Task 的当前状态
     */
    private final Map<String, ExecutionState> taskStates = new ConcurrentHashMap<>();

    /**
     * 初始化一个 Task 的状态。
     */
    public void deployTask(String taskId) {
        // 模拟 Task 部署，初始状态为 DEPLOYING
        taskStates.put(taskId, ExecutionState.DEPLOYING);

        // (模拟) 部署成功后，异步更新为 RUNNING
        // 注意：这里我们手动将其设置为 RUNNING，以准备好并发挑战
        taskStates.put(taskId, ExecutionState.RUNNING);
        log.info("SYSTEM: {} is now {}", taskId, ExecutionState.RUNNING);
    }

    /**
     * 尝试原子性地更新一个 Task 的状态。
     *
     * @param taskId   要更新的 Task ID
     * @param newState 尝试变更的 *新* 状态
     * @return true 如果状态更新成功, false 如果更新失败（例如，因为状态不允许转换）
     */
    public boolean attemptStateUpdate(String taskId, ExecutionState newState) {

        // =========================================================================
        // TODO: 在这里实现你的原子更新逻辑
        //
        // 你需要使用 ConcurrentHashMap 的一个原子方法来实现以下逻辑：
        // 1. 获取 taskId 对应的 "currentState"。
        // 2. 检查状态转换是否有效（见下方的 canTransition() 辅助方法）。
        // 3. 如果有效，就原子性地将新状态 (newState) 写入 Map，并返回 true。
        // 4. 如果无效（或 key 不存在），则不写入，并返回 false。
        //
        // 提示：你需要一个能 "读取-计算-写入" 都在一个原子操作中完成的方法。
        // =========================================================================

        // 示例：一个 *错误* 的、*非原子* 的实现 (请替换掉它)
        /*
        ExecutionState currentState = taskStates.get(taskId);
        if (canTransition(currentState, newState)) {
            taskStates.put(taskId, newState); // <-- 非原子！在 get() 和 put() 之间可能被其他线程修改
            return true;
        }
        return false;
        */

        // 请将你的实现写在这里...
        AtomicBoolean success = new AtomicBoolean(false);
        taskStates.compute(taskId, (id, currentState) -> {
            if (canTransition(currentState, newState)) {
                success.set(true);
                return newState;
            } else {
                success.set(false);
                return currentState;
            }
        });
        return success.get(); // 临时
    }

    /**
     * 辅助方法：检查状态转换是否有效。
     * (这是简化的 Flink 状态机规则)
     */
    private boolean canTransition(ExecutionState currentState, ExecutionState newState) {
        if (currentState == null) {
            return false; // Task 不存在
        }

        // 规则 1: 终止状态 (FINISHED, FAILED, CANCELED) 不能再被改变
        if (currentState == ExecutionState.FINISHED ||
                currentState == ExecutionState.FAILED ||
                currentState == ExecutionState.CANCELED) {
            return false;
        }

        // 规则 2: 允许的转换
        if (currentState == ExecutionState.RUNNING) {
            return newState == ExecutionState.FINISHED ||
                    newState == ExecutionState.FAILED ||
                    newState == ExecutionState.CANCELING;
        }

        if (currentState == ExecutionState.DEPLOYING) {
            return newState == ExecutionState.RUNNING ||
                    newState == ExecutionState.FAILED;
        }

        // 其他转换... (为保持简单，我们只关注上述情况)
        return false;
    }

    /**
     * 获取 Task 的最终状态
     */
    public ExecutionState getTaskState(String taskId) {
        return taskStates.get(taskId);
    }

    // =========================================================================
    // main 方法：模拟并发挑战
    // =========================================================================
    public static void main(String[] args) throws InterruptedException {
        TaskStateTracker tracker = new TaskStateTracker();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        final String taskId = "task-A";
        tracker.deployTask(taskId); // Task-A 进入 RUNNING 状态

        // 模拟并发事件：
        // 线程 1: TaskExecutor 报告 "Task 成功"
        Runnable taskSuccess = () -> {
            log.info("THREAD-1: 尝试将 {} 设置为 {}...", taskId, ExecutionState.FINISHED);
            boolean success = tracker.attemptStateUpdate(taskId, ExecutionState.FINISHED);
            log.info("THREAD-1: 更新结果: {}", success);
        };

        // 线程 2: JobManager (用户) 报告 "Task 取消"
        Runnable taskCancel = () -> {
            log.info("THREAD-2: 尝试将 {} 设置为 {}...", taskId, ExecutionState.CANCELING);
            boolean success = tracker.attemptStateUpdate(taskId, ExecutionState.CANCELING);
            log.info("THREAD-2: 更新结果: {}", success);
        };

        // 并发执行
        executor.submit(taskSuccess);
        executor.submit(taskCancel);

        // 等待执行绪完成
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // 检查最终状态
        // 无论线程 1 和 2 谁先执行，最终状态必须是 FINISHED 或 CANCELING 之一。
        // 它 *绝不能* 是 RUNNING。
        // 并且，如果你的逻辑正确，两个线程的 update 结果应该一个是 true，一个是 false。
        log.info("========================================");
        log.info("最终状态: {} is {}", taskId, tracker.getTaskState(taskId));
        log.info("========================================");
    }
}