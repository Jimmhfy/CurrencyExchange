import org.example.currencyexchange.utils.CircuitBreaker;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
    # Overview

    Implement a Circuit Breaker pattern to prevent cascading failures when calling external services.
    The circuit breaker counts failures and stops calling a failing service after a threshold is reached.

    ## Requirements

    Implement a circuit breaker with two states:

    - Passing (default): Function calls pass through normally
    - Blocking: Function calls are blocked, returning immediate errors

    ## Functionality:

    - Accept a function to protect and a failure threshold
    - Track consecutive failures
    - When passing: Execute the function and return its result
    - When failures reach threshold: Switch to blocking state
    - When blocking: Return errors without calling the function

    ## Basic Usage Example:

    ``` pseudo
    val circuitBreaker = CircuitBreaker(callExternalService, failureThreshold = 3)
    val result = circuitBreaker.execute(parameters)
    ```
*/


class CircuitBreakerBlockException extends RuntimeException {
    CircuitBreakerBlockException(String msg) {
        super(msg);
    }
}

public class CircuitBreakerAnswer<I,R> {
    private State state;

    enum State {
        PASSING,
        BLOCK
    }

    private int failureCount = 0;
    private final int failureThreshold;
    private final Function<I, R> function;

    public CircuitBreakerAnswer(Function<I, R> function, int failureThreshold) {
        this.failureThreshold = failureThreshold;
        this.function = function;
        state = State.PASSING;
    }

    public R execute(I input) {
        if (isBlocked()) throw new CircuitBreakerBlockException("Circuit Breaker Blocked!");
        try {
            R result = this.function.apply(input);
            successfulReset();
            return result;
        } catch (Exception e) {
            incrementFailureCount();
            if (shouldBlockStage()) state = State.BLOCK;
            throw e;
        }
    }

    public void successfulReset() {
        failureCount = 0;
    }

    public void incrementFailureCount() {
        failureCount++;
    }

    public boolean shouldBlockStage() {
        return failureCount >= failureThreshold;
    }

    public boolean isBlocked() {
        return this.state == State.BLOCK;
    }
}

