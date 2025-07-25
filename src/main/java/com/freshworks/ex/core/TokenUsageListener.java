package com.freshworks.ex.core;

import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class TokenUsageListener implements ChatModelListener {

    private int totalInputTokens = 0;
    private int totalOutputTokens = 0;
    private double totalCost = 0.0;

    private int lastInputTokens = 0;
    private int lastOutputTokens = 0;
    private double lastRequestCost = 0.0;

    private static final Logger logger = LoggerFactory.getLogger(TokenUsageListener.class);

    // Azure GPT-4.1 pricing (2024)
    private static final double INPUT_COST_PER_TOKEN = 0.002 / 1000;  // $0.002 per 1K tokens
    private static final double OUTPUT_COST_PER_TOKEN = 0.008 / 1000; // $0.008 per 1K tokens

    @Override
    public void onRequest(ChatModelRequestContext request) {
        // Optional: Can capture metadata or timestamp here
    }

    @Override
    public void onResponse(ChatModelResponseContext response) {
        if (response.chatResponse().tokenUsage() != null) {
            lastInputTokens = response.chatResponse().tokenUsage().inputTokenCount();
            lastOutputTokens = response.chatResponse().tokenUsage().outputTokenCount();

            totalInputTokens += lastInputTokens;
            totalOutputTokens += lastOutputTokens;

            lastRequestCost = (lastInputTokens * INPUT_COST_PER_TOKEN) +
                    (lastOutputTokens * OUTPUT_COST_PER_TOKEN);

            totalCost += lastRequestCost;

            logger.debug("Token usage - Input: {}, Output: {}, Cost: ${}",
                    lastInputTokens, lastOutputTokens, String.format("%.6f", lastRequestCost));
        }
    }

    public void reset() {
        totalInputTokens = 0;
        totalOutputTokens = 0;
        totalCost = 0.0;
        lastInputTokens = 0;
        lastOutputTokens = 0;
        lastRequestCost = 0.0;
    }
}