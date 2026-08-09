package org.example.currencyexchange.task;

import lombok.Getter;

import java.util.concurrent.Callable;

@Getter
public class RateFetchTask implements Callable {
    private double result;


    @Override
    public Object call() throws Exception {
        result = 10.5;
        return result;
    }
}
