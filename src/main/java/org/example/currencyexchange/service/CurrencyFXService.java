package org.example.currencyexchange.service;

import org.example.currencyexchange.model.FXPair;
import org.example.currencyexchange.model.FXRate;
import org.example.currencyexchange.task.RateFetchFunctionTask;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.function.Function;

@Service
public class CurrencyFXService {
    private final ExecutorService executorService;
    private final HTTPRequestService httpRequestService;
    private HashMap<FXPair, FXRate> ratesCache;
    public CurrencyFXService(HTTPRequestService httpRequestService) {
        ThreadFactory factory = runnable -> {
            Thread t = new Thread(runnable);
            t.setName("FX Service Thread" + t.getName());
            t.setUncaughtExceptionHandler((thread, ex) -> {
                System.out.println("Uncatch Exception from "+ thread.getName()+" "+ ex.getCause().getMessage());
            });
            return t;
        };

        BlockingDeque<Runnable> workQueue = new LinkedBlockingDeque<>();
        executorService = new ThreadPoolExecutor(
                4,
                8,
                60L, TimeUnit.SECONDS,
                workQueue,
                factory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        this.httpRequestService = httpRequestService;
    }

    public <T> Future<T> submitRateCalculationTask(Callable<T> calculationTask){
        if (calculationTask == null) {
            return CompletableFuture.completedFuture(null);
        }
        return executorService.submit(calculationTask);
    }

    public BigDecimal getRates(String base, String toCurrency) throws IOException, InterruptedException {
        if (base.equals(toCurrency)) return BigDecimal.ONE;

        FXPair fromPair = new FXPair("USD", base);
        FXPair toPair = new FXPair("USD", toCurrency);

        if (ratesCache == null || !ratesCache.containsKey(fromPair) || !ratesCache.containsKey(toPair) || isExpire(toPair) || isExpire(fromPair)) {
            ratesCache = httpRequestService.requestRate();
            if (!ratesCache.containsKey(fromPair) || !ratesCache.containsKey(toPair))
                throw new IOException("No rate available");
        }

        BigDecimal dividend = ratesCache.get(toPair).rate();
        BigDecimal divisor = ratesCache.get(fromPair).rate();

        return dividend.divide(divisor, 8, RoundingMode.HALF_UP);
    }

    public boolean isExpire(FXPair currencyPair) {
        return ratesCache.get(currencyPair).expireAt().isBefore(Instant.now());
    }
}
