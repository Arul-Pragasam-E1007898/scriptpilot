package com.freshworks.ex.scenarios;

// Simple POJO class for test case
public class TestCase {
    private final String id;
    private final String key;
    private final String steps;
    private boolean status;
    private long duation;

    public TestCase(String id, String key, String steps) {
        this.id = id;
        this.key = key;
        this.steps = steps;
        this.status = false;
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getSteps() {
        return steps;
    }

    public boolean getStatus() {
        return status;
    }

    public long getDuration() {
        return duation;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setDuration(long duation) {
        this.duation = duation;
    }
}