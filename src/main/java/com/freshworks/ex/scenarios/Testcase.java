package com.freshworks.ex.scenarios;

public record Testcase(Integer id, String steps, Category category, String systemMessage) {

	public static Testcase newCase(Integer id, String steps, Category category, String systemMessage) {
		return new Testcase(id, steps, category, systemMessage);
	}

	public static Testcase newCase(Integer id, String steps, Category category) {
		return new Testcase(id, steps, category, null);
	}
}
