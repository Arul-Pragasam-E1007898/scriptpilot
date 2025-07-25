package com.freshworks.ex.utils;

import com.freshworks.ex.scenarios.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HtmlReportGenerator {
    private static final Logger logger = LoggerFactory.getLogger(HtmlReportGenerator.class);
    private static final String DEFAULT_REPORT_FILENAME = "test-execution-report.html";

    // Azure GPT-4.1 Pricing
    private static final double INPUT_COST_PER_TOKEN = 0.000002;
    private static final double OUTPUT_COST_PER_TOKEN = 0.000008;

    public static void generateReport(List<TestCase> testCases) {
        try {
            generateReport(testCases, DEFAULT_REPORT_FILENAME);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void generateReport(List<TestCase> testCases, String filename) throws IOException {
        logger.info("Generating HTML report for {} test cases", testCases.size());

        long totalTests = testCases.size();
        long passedTests = testCases.stream().filter(TestCase::isStatus).count();
        long failedTests = totalTests - passedTests;
        double passRate = totalTests > 0 ? (double) passedTests / totalTests * 100 : 0;
        long totalDuration = testCases.stream().mapToLong(TestCase::getDuration).sum();

        long totalInputTokens = testCases.stream().mapToLong(TestCase::getInputTokens).sum();
        long totalOutputTokens = testCases.stream().mapToLong(TestCase::getOutputTokens).sum();
        double totalCost = totalInputTokens * INPUT_COST_PER_TOKEN + totalOutputTokens * OUTPUT_COST_PER_TOKEN;

        StringBuilder html = new StringBuilder();
        html.append(buildHtmlHeader());
        html.append(buildHeaderSection());
        html.append(buildSummarySection(totalTests, passedTests, failedTests, passRate, totalDuration));
        html.append(buildTokenSummary(totalInputTokens, totalOutputTokens, totalCost));
        html.append(buildTestCaseTable(testCases));
        html.append(buildHtmlFooter());

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(html.toString());
        }

        logger.info("HTML report generated successfully: {}", filename);
    }

    private static String buildHeaderSection() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm:ss"));
        return String.format("""
            <div class="header">
                <h1>🚀 ScriptPilot Test Report</h1>
                <p>Generated on %s</p>
            </div>
        """, timestamp);
    }

    private static String buildSummarySection(long total, long passed, long failed, double passRate, long duration) {
        double durationMinutes = duration / 60.0;
        String durationDisplay = durationMinutes < 1.0 ?
                String.format("%.1fs", (double) duration) :
                String.format("%.1fm", durationMinutes);

        return String.format("""
            <div class="summary">
                <div class="summary-card"><h3 class="total">%d</h3><p>Total Tests</p></div>
                <div class="summary-card"><h3 class="passed">%d</h3><p>Passed</p></div>
                <div class="summary-card"><h3 class="failed">%d</h3><p>Failed</p></div>
                <div class="summary-card"><h3 class="rate">%.1f%%</h3><p>Pass Rate</p></div>
                <div class="summary-card"><h3 class="duration">%s</h3><p>Total Duration</p></div>
            </div>
        """, total, passed, failed, passRate, durationDisplay);
    }

    private static String buildTokenSummary(long inputTokens, long outputTokens, double cost) {
        return String.format("""
            <div class="summary">
                <div class="summary-card"><h3 class="total">%d</h3><p>Total Input Tokens</p></div>
                <div class="summary-card"><h3 class="total">%d</h3><p>Total Output Tokens</p></div>
                <div class="summary-card"><h3 class="rate">$%.4f</h3><p>Total Token Cost</p></div>
            </div>
        """, inputTokens, outputTokens, cost);
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
                                <th>#</th>
                                <th>Test Key</th>
                                <th>Status</th>
                                <th>Duration (s)</th>
                                <th>Steps</th>
                                <th>Token(input)</th>
                                <th>Token(output)</th>
                                <th>Cost ($)</th>
                            </tr>
                        </thead>
                        <tbody>
        """);

        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            String statusClass = testCase.isStatus() ? "status-passed" : "status-failed";
            String statusText = testCase.isStatus() ? "✅ Passed" : "❌ Failed";
            double inputCost = testCase.getInputTokens() * INPUT_COST_PER_TOKEN;
            double outputCost = testCase.getOutputTokens() * OUTPUT_COST_PER_TOKEN;
            double cost = inputCost + outputCost;
            String steps = testCase.getSteps();  // Allow raw HTML

            table.append(String.format("""
                <tr>
                    <td>%d</td>
                    <td><a href="https://freshworks.freshrelease.com/ws/FS/test-cases/%s" target="_blank" class="test-key-link"><strong>%s</strong></a></td>
                    <td><span class="status-badge %s">%s</span></td>
                    <td>%d</td>
                    <td class="steps-cell">
                        <div class="steps-content">
                            <div class="steps-preview" id="preview-%d">%s</div>
                            <div class="steps-full" id="full-%d" style="display: none;">%s</div>
                            <button class="steps-toggle" onclick="toggleSteps(%d)">Show More</button>
                        </div>
                    </td>
                    <td>%d</td>
                    <td>%d</td>
                    <td>$%.4f</td>
                </tr>
            """, i + 1, testCase.getKey(), testCase.getKey(), statusClass, statusText,
                    testCase.getDuration(), i, steps, i, steps, i,
                    testCase.getInputTokens(), testCase.getOutputTokens(), cost));
        }

        table.append("""
                        </tbody>
                    </table>
                </div>
            </div>
        """);

        return table.toString();
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
                * { margin: 0; padding: 0; box-sizing: border-box; }
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
                .content { padding: 30px; }
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
                    vertical-align: top;
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
                    max-width: 500px;
                    word-wrap: break-word;
                    font-size: 0.9rem;
                    line-height: 1.4;
                }
                .steps-content {
                    position: relative;
                }
                .steps-preview {
                    max-height: 4.2em;
                    overflow: hidden;
                    position: relative;
                }
                .steps-preview::after {
                    content: '';
                    position: absolute;
                    bottom: 0;
                    left: 0;
                    right: 0;
                    height: 1.4em;
                    background: linear-gradient(transparent, white);
                }
                .steps-full {
                    display: none;
                }
                .steps-toggle {
                    background: #007bff;
                    color: white;
                    border: none;
                    padding: 4px 8px;
                    border-radius: 4px;
                    font-size: 0.8rem;
                    cursor: pointer;
                    margin-top: 8px;
                    transition: background-color 0.2s;
                }
                .steps-toggle:hover {
                    background: #0056b3;
                }
                .steps-content p { margin: 0.5em 0; }
                .steps-content ul, .steps-content ol {
                    margin: 0.5em 0;
                    padding-left: 1.5em;
                }
                .steps-content li { margin: 0.3em 0; }
                .steps-content code {
                    background: #f1f3f4;
                    padding: 2px 4px;
                    border-radius: 3px;
                    font-family: 'Courier New', monospace;
                    font-size: 0.85em;
                }
                .steps-content strong { font-weight: 600; }
                .test-key-link {
                    color: #007bff;
                    text-decoration: none;
                    transition: color 0.2s;
                }
                .test-key-link:hover {
                    color: #0056b3;
                    text-decoration: underline;
                }
                .test-key-link:visited {
                    color: #6f42c1;
                }
                .footer {
                    text-align: center;
                    padding: 20px;
                    background: #f8f9fa;
                    color: #666;
                    font-size: 0.9rem;
                }
                @media (max-width: 768px) {
                    .header h1 { font-size: 2rem; }
                    .summary { grid-template-columns: 1fr; }
                    .content { padding: 20px; }
                    th, td { padding: 10px; }
                    .steps-cell { max-width: 300px; }
                }
            </style>
        </head>
        <body>
        <div class="container">
    """;
    }

    private static String buildHtmlFooter() {
        return """
            <div class="footer">
                <p>Generated by ScriptPilot Test Automation Framework</p>
            </div>
            </div>
            <script>
                function toggleSteps(index) {
                    const preview = document.getElementById('preview-' + index);
                    const full = document.getElementById('full-' + index);
                    const button = preview.parentElement.querySelector('.steps-toggle');
                    if (full.style.display === 'none' || full.style.display === '') {
                        preview.style.display = 'none';
                        full.style.display = 'block';
                        button.textContent = 'Show Less';
                    } else {
                        preview.style.display = 'block';
                        full.style.display = 'none';
                        button.textContent = 'Show More';
                    }
                }
                document.addEventListener('DOMContentLoaded', function () {
                    const allFull = document.querySelectorAll('.steps-full');
                    allFull.forEach(function (element) {
                        element.style.display = 'none';
                    });
                });
            </script>
            </body>
            </html>
        """;
    }
}
