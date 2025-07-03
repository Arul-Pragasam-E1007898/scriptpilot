package com.freshworks.ex.utils;

import com.freshworks.ex.scenarios.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for generating HTML test execution reports.
 * This class creates a comprehensive HTML report with test case results,
 * execution statistics, and a modern responsive design.
 */
public class HtmlReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(HtmlReportGenerator.class);
    
    private static final String DEFAULT_REPORT_FILENAME = "test-execution-report.html";
    
    /**
     * Generates an HTML report for the given test cases using the default filename.
     * 
     * @param testCases List of executed test cases
     * @throws IOException if the report file cannot be written
     */
    public static void generateReport(List<TestCase> testCases) {
        try {
            generateReport(testCases, DEFAULT_REPORT_FILENAME);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Generates an HTML report for the given test cases.
     * 
     * @param testCases List of executed test cases
     * @param filename Name of the HTML report file to generate
     * @throws IOException if the report file cannot be written
     */
    public static void generateReport(List<TestCase> testCases, String filename) throws IOException {
        logger.info("Generating HTML report for {} test cases", testCases.size());
        
        StringBuilder html = new StringBuilder();
        
        // Calculate statistics
        long totalTests = testCases.size();
        long passedTests = testCases.stream().mapToLong(tc -> tc.getStatus() ? 1 : 0).sum();
        long failedTests = totalTests - passedTests;
        double passRate = totalTests > 0 ? (double) passedTests / totalTests * 100 : 0;
        long totalDuration = testCases.stream().mapToLong(TestCase::getDuration).sum();
        
        // Build HTML content
        html.append(buildHtmlHeader());
        html.append(buildReportTitle());
        html.append(buildExecutionSummary(totalTests, passedTests, failedTests, passRate, totalDuration));
        html.append(buildTestCaseTable(testCases));
        html.append(buildHtmlFooter());
        
        // Write to file
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(html.toString());
        }
        
        logger.info("HTML report generated successfully: {}", filename);
    }
    
    private static String buildHtmlHeader() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>ScriptPilot Test Execution Report</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        background-color: #f5f5f5;
                        padding: 20px;
                    }
                    
                    .container {
                        max-width: 1200px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 10px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    
                    .header {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    
                    .header h1 {
                        font-size: 2.5rem;
                        margin-bottom: 10px;
                    }
                    
                    .header p {
                        font-size: 1.1rem;
                        opacity: 0.9;
                    }
                    
                    .summary {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                        gap: 20px;
                        padding: 30px;
                        background: #f8f9fa;
                    }
                    
                    .summary-card {
                        background: white;
                        padding: 20px;
                        border-radius: 8px;
                        text-align: center;
                        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                    }
                    
                    .summary-card h3 {
                        font-size: 2rem;
                        margin-bottom: 5px;
                    }
                    
                    .summary-card p {
                        color: #666;
                        font-size: 0.9rem;
                    }
                    
                    .passed { color: #28a745; }
                    .failed { color: #dc3545; }
                    .total { color: #007bff; }
                    .duration { color: #6f42c1; }
                    .rate { color: #17a2b8; }
                    
                    .content {
                        padding: 30px;
                    }
                    
                    .table-container {
                        overflow-x: auto;
                        margin-top: 20px;
                    }
                    
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        background: white;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                    }
                    
                    th, td {
                        padding: 15px;
                        text-align: left;
                        border-bottom: 1px solid #eee;
                    }
                    
                    th {
                        background: #f8f9fa;
                        font-weight: 600;
                        color: #555;
                    }
                    
                    tr:hover {
                        background: #f8f9fa;
                    }
                    
                    .status-badge {
                        padding: 4px 12px;
                        border-radius: 20px;
                        font-size: 0.85rem;
                        font-weight: 500;
                        text-transform: uppercase;
                    }
                    
                    .status-passed {
                        background: #d4edda;
                        color: #155724;
                    }
                    
                    .status-failed {
                        background: #f8d7da;
                        color: #721c24;
                    }
                    
                    .steps-cell {
                        max-width: 400px;
                        word-wrap: break-word;
                        white-space: pre-wrap;
                        font-size: 0.9rem;
                        line-height: 1.4;
                    }
                    
                    .footer {
                        text-align: center;
                        padding: 20px;
                        background: #f8f9fa;
                        color: #666;
                        font-size: 0.9rem;
                    }
                    
                    @media (max-width: 768px) {
                        .header h1 {
                            font-size: 2rem;
                        }
                        
                        .summary {
                            grid-template-columns: 1fr;
                        }
                        
                        .content {
                            padding: 20px;
                        }
                        
                        th, td {
                            padding: 10px;
                        }
                    }
                </style>
            </head>
            <body>
                <div class="container">
            """;
    }
    
    private static String buildReportTitle() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm:ss"));
        return String.format("""
            <div class="header">
                <h1>🚀 ScriptPilot Test Report</h1>
                <p>Generated on %s</p>
            </div>
            """, timestamp);
    }
    
    private static String buildExecutionSummary(long totalTests, long passedTests, long failedTests, 
                                               double passRate, long totalDuration) {
        return String.format("""
            <div class="summary">
                <div class="summary-card">
                    <h3 class="total">%d</h3>
                    <p>Total Tests</p>
                </div>
                <div class="summary-card">
                    <h3 class="passed">%d</h3>
                    <p>Passed</p>
                </div>
                <div class="summary-card">
                    <h3 class="failed">%d</h3>
                    <p>Failed</p>
                </div>
                <div class="summary-card">
                    <h3 class="rate">%.1f%%</h3>
                    <p>Pass Rate</p>
                </div>
                <div class="summary-card">
                    <h3 class="duration">%ds</h3>
                    <p>Total Duration</p>
                </div>
            </div>
            """, totalTests, passedTests, failedTests, passRate, totalDuration);
    }
    
    private static String buildTestCaseTable(List<TestCase> testCases) {
        StringBuilder table = new StringBuilder();
        table.append("""
            <div class="content">
                <h2>Test Case Details</h2>
                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Test ID</th>
                                <th>Test Key</th>
                                <th>Status</th>
                                <th>Duration (s)</th>
                                <th>Steps</th>
                            </tr>
                        </thead>
                        <tbody>
            """);
        
        for (TestCase testCase : testCases) {
            String statusClass = testCase.getStatus() ? "status-passed" : "status-failed";
            String statusText = testCase.getStatus() ? "✅ Passed" : "❌ Failed";
            String steps = escapeHtml(testCase.getSteps());
            
            table.append(String.format("""
                <tr>
                    <td>%s</td>
                    <td><strong>%s</strong></td>
                    <td><span class="status-badge %s">%s</span></td>
                    <td>%d</td>
                    <td class="steps-cell">%s</td>
                </tr>
                """, 
                escapeHtml(testCase.getId()),
                escapeHtml(testCase.getKey()),
                statusClass,
                statusText,
                testCase.getDuration(),
                steps));
        }
        
        table.append("""
                        </tbody>
                    </table>
                </div>
            </div>
            """);
        
        return table.toString();
    }
    
    private static String buildHtmlFooter() {
        return """
            <div class="footer">
                <p>Generated by ScriptPilot Test Automation Framework</p>
            </div>
        </div>
    </body>
    </html>
    """;
    }
    
    /**
     * Escapes HTML special characters to prevent XSS and formatting issues.
     * 
     * @param text The text to escape
     * @return HTML-escaped text
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
} 