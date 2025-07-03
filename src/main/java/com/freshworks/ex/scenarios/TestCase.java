package com.freshworks.ex.scenarios;

// Simple POJO class for test case
public class TestCase {
    private final String id;
    private final String key;
    private final String steps;
    private boolean status;

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

    public void setStatus(boolean status) {
        this.status = status;
    }
}