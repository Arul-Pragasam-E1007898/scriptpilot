package com.freshworks.ex.scenarios;

public record Testcase(Integer id, String steps, Category category, String systemMessage, boolean enabled) {

    public static Testcase newCase(Integer id, String steps, Category category, String systemMessage, boolean enabled) {
        return new Testcase(id, steps, category, systemMessage, enabled);
    }

    public static Testcase newCase(Integer id, String steps, Category category, boolean enabled) {
        return new Testcase(id, steps, category, null, enabled);
    }

    public static Testcase newCase(Integer id, String steps, Category category, String systemMessage) {
        return new Testcase(id, steps, category, systemMessage, true);
    }

    public static Testcase newCase(Integer id, String steps, Category category) {
        return new Testcase(id, steps, category, null, true);
    }
}
