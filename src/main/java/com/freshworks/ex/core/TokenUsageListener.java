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
    private static final Logger logger = LoggerFactory.getLogger(TokenUsageListener.class);

    // Claude pricing (check current rates)
    private static final double INPUT_COST_PER_TOKEN = 0.000003; // $3 per 1M tokens
    private static final double OUTPUT_COST_PER_TOKEN = 0.000015; // $15 per 1M tokens

    @Override
    public void onRequest(ChatModelRequestContext request) {
        // Optional
    }

    @Override
    public void onResponse(ChatModelResponseContext response) {
        if (response.chatResponse().tokenUsage() != null) {
            lastInputTokens = response.chatResponse().tokenUsage().inputTokenCount();
            lastOutputTokens = response.chatResponse().tokenUsage().outputTokenCount();

            totalInputTokens += lastInputTokens;
            totalOutputTokens += lastOutputTokens;

            double requestCost = (lastInputTokens * INPUT_COST_PER_TOKEN) +
                    (lastOutputTokens * OUTPUT_COST_PER_TOKEN);
            totalCost += requestCost;

            logger.debug("Request - Input: {}, Output: {}, Cost: ${}", lastInputTokens, lastOutputTokens,
                    String.format("%.6f", requestCost));
        }
    }

    public void reset() {
        totalInputTokens = 0;
        totalOutputTokens = 0;
        totalCost = 0.0;
        lastInputTokens = 0;
        lastOutputTokens = 0;
    }
}