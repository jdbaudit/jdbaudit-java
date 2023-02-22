package com.jthinking.jdbaudit.scan.api.entity;

public class TaskControl {

    /**
     * stop flag
     */
    private volatile boolean stop;

    public boolean isStop() {
        return stop;
    }

    public void start() {
        this.stop = false;
    }

    public void stop() {
        this.stop = true;
    }
}
