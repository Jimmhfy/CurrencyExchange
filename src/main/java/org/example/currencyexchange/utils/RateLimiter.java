package org.example.currencyexchange.utils;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class RateLimiter {
    private final int maxToken;
    private long lastCall = System.currentTimeMillis();
    private final int windowSize;
    private AtomicInteger tokenCount;

    public RateLimiter (int windowSize, int maxToken) {
        this.windowSize = windowSize;
        this.maxToken = maxToken;
    }

    public <I,R> R call(Function<I,R> functionCall, I input) {
        fillToken();
        if (tokenCount.get() <= 0) throw new RuntimeException("Out of Token");
        tokenCount.decrementAndGet();
        return functionCall.apply(input);
    }

    public void fillToken() {
        int fillToken = (int) ((System.currentTimeMillis() - lastCall) / windowSize) * maxToken;
        if (fillToken > 0)
            tokenCount.addAndGet(Math.min(maxToken, tokenCount.addAndGet(fillToken)));
        lastCall = System.currentTimeMillis();
    }
}
