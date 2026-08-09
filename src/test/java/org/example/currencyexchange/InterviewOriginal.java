package org.example.currencyexchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

class InterviewOriginal {

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

    // class -> CircuitBreaker
    //      need instance
    //  static state {0,1} => final variable pass = 2, failure = 1, block = 0;
    //  static failureThreshold = 0;
    //  static maxThreashold = Integer.MAX_VALUE;
    // construct ()

    // function execute(Callable fucntion);

    public class CircuitBreaker {
        final int PASSING = 2;
        final int FAILURE = 1;
        final int BLOCK = 0;

        private int state = 2;
        private int failureThreashold = 0;
        private int maxThreadshold = Integer.MAX_VALUE;

        public CircuitBreaker(int failureThreashold) {
            maxThreadshold = failureThreashold;
        }

        public Object execute(Callable function) {
            Object output;
            try {
                if (state == BLOCK) throw new RuntimeException();
                output = function.call();
                System.out.println("Success");
            } catch (Exception e) {
                /*
                - When passing: Execute the function and return its result
                - When failures reach threshold: Switch to blocking state
                - When blocking: Return errors without calling the function
                 */
                failureThreashold++;
                switch (state) {
                    case PASSING: {
                        state = BLOCK;
                    }
                    case FAILURE: {
                        if (failureThreashold >= maxThreadshold)
                            state = BLOCK;
                    }
                }
                System.out.println("Failure");
                throw new RuntimeException();
            }
            return output;
        }
    }

    CircuitBreaker circuitBreaker;

    @BeforeEach
    public void init() {
        circuitBreaker = new CircuitBreaker(3);
    }

    @Test
    public void circuitBreaker_SUCCESS() {
        var output = circuitBreaker.execute(() -> {
            return 1 + 1;
        });
        var expected = 2;
        assertEquals(expected, output);
    }

    @Test
    public void circuitBreaker_FAILURE() {
        for (int i = 0; i < 3; i++) {
            try {
                var output = circuitBreaker.execute(() -> {
                    throw new RuntimeException();
                });
            } catch (Exception e) {
                assertInstanceOf(RuntimeException.class, e);
            }
        }

        assertThrows(RuntimeException.class, () -> {
            circuitBreaker.execute(() -> {
                return 1 + 1;
            });
        });
    }
}