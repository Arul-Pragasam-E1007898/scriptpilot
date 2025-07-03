package com.freshworks.ex;

import com.freshworks.ex.core.ScriptRunner;
import com.freshworks.ex.scenarios.TestCase;
import com.freshworks.ex.scenarios.TestCaseLoader;
import com.freshworks.ex.utils.HtmlReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class ScriptPilot {
    private static final Logger logger = LoggerFactory.getLogger(ScriptPilot.class);
    public static ScriptRunner runner = new ScriptRunner();


    public static void main(String[] args) throws IOException {
        logger.info("Starting ScriptPilot application");

        TestCaseLoader testCaseLoader = new TestCaseLoader();
        List<TestCase> testCases = testCaseLoader.fetch();
        testCases.sort(Comparator.comparing(TestCase::getKey));

        runner.execute(testCases);

        // Generate HTML report after all test cases are executed
        HtmlReportGenerator.generateReport(testCases);
    }

}
