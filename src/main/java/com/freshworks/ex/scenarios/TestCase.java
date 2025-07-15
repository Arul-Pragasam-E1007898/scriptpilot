package com.freshworks.ex.scenarios;

import lombok.Data;
import lombok.Getter;

// Simple POJO class for test case
@Getter
@Data
public class TestCase {
    private final String id;
    private final String key;
    private final String steps;
    private boolean status;
    private long duration;
    private int inputTokens;
    private int outputTokens;

    public TestCase(String id, String key, String steps) {
        this.id = id;
        this.key = key;
        this.steps = steps;
        this.status = false;
    }
}