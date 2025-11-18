package com.liboshuai.demo.juc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TaskRegistry {

    // 存储 TaskID -> Status
    private final Map<String, String> taskStatusMap = new ConcurrentHashMap<>();

    /**
     * 注册新任务
     * @param taskId 任务ID
     * @return 如果是新注册的，返回 "CREATED"；如果已经存在，返回现有的状态
     */
    public String registerTask(String taskId) {
        // TODO: 请使用原子操作实现 "Check-Then-Act"
        // 提示：不要用 if (map.containsKey) { ... } else { map.put ... }
        // 思考：putIfAbsent 和 computeIfAbsent 有什么区别？这里用哪个更好？
        /*
        思考回答:
            1. putIfAbsent 和 computeIfAbsent 有什么区别？
                putIfAbsent不支持Lambda表达式, 只能传入一个确定的值, 用于key不存在时放入map.
                而computeIfAbsent支持Lambda表达式, 可以传入一段创建值的逻辑代码, 其返回值用于key不存在时放入map. 更加灵活.
            2. 这里只需要返回"CREATED"这个确定值即可, 不需要进行其他复杂的计算操作, 所以使用 putIfAbsent 更好.
         */
        String status = taskStatusMap.putIfAbsent(taskId, "CREATED");
        String finalStatus;
        if (status == null) {
            finalStatus = "CREATED";
        } else {
            finalStatus = status;
        }
        return finalStatus;
    }

    public void updateStatus(String taskId, String status) {
        taskStatusMap.put(taskId, status);
    }

    public String getStatus(String taskId) {
        return taskStatusMap.get(taskId);
    }
}