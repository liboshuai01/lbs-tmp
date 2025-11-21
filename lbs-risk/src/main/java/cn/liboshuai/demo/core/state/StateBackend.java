package cn.liboshuai.demo.core.state;

import cn.liboshuai.demo.core.window.SlidingWindowCounter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 状态后端接口，支持扩展 (Memory, Redis, RocksDB...)
 */
public interface StateBackend {
    /**
     * 初始化并恢复状态
     * @return 恢复后的内存状态 Map
     */
    ConcurrentHashMap<String, SlidingWindowCounter> load();

    /**
     * 执行快照保存
     * @param stateMap 当前内存中的状态快照
     */
    void snapshot(Map<String, SlidingWindowCounter> stateMap);

    void close();
}