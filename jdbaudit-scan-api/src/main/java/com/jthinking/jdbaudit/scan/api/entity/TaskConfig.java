package com.jthinking.jdbaudit.scan.api.entity;

public class TaskConfig {

    /**
     * 默认正在运行的最大任务数
     */
    private static final int DEFAULT_MAX_RUNNING_TASK_NUM = 100;

    /**
     * 默认任务超时时间。单位：毫秒
     */
    private static final long DEFAULT_TASK_TIMEOUT = 1000 * 60 * 10;

    private int maxRunningTask;

    private long taskTimeout;

    public TaskConfig() {
        this.maxRunningTask = DEFAULT_MAX_RUNNING_TASK_NUM;
        this.taskTimeout = DEFAULT_TASK_TIMEOUT;
    }

    public TaskConfig(int maxRunningTask, long taskTimeout) {
        this.maxRunningTask = maxRunningTask;
        this.taskTimeout = taskTimeout;
    }

    public int getMaxRunningTask() {
        return maxRunningTask;
    }

    public void setMaxRunningTask(int maxRunningTask) {
        this.maxRunningTask = maxRunningTask;
    }

    public long getTaskTimeout() {
        return taskTimeout;
    }

    public void setTaskTimeout(int taskTimeout) {
        this.taskTimeout = taskTimeout;
    }
}
