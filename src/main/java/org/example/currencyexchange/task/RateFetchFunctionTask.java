package org.example.currencyexchange.task;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.function.Function;

public class RateFetchFunctionTask<T> implements Callable<T> {
    private final String concurrency;
    private final Function<String, T> task;

    public RateFetchFunctionTask(String concurrency, Function<String, T> task) {
        this.concurrency = concurrency;
        this.task = task;
    }

    @Override
    public T call() throws Exception {
        return task.apply(concurrency);
    }
}
