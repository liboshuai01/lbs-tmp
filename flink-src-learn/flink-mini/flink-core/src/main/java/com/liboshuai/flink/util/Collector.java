package com.liboshuai.flink.util;

public interface Collector<T> {
    /**
     * Emits a record.
     *
     * @param record The record to collect.
     */
    void collect(T record);

    /** Closes the collector. If any data was buffered, that data will be flushed. */
    void close();
}
