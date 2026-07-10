package org.example.currencyexchange.utils;

import org.example.currencyexchange.exception.CircuitBreakerException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CircuitBreakerTest {
    static CircuitBreaker circuitBreaker;
    @BeforeAll
    public static void setup() {
        circuitBreaker = new CircuitBreaker(3);
    }

    @Test
    public void functionalInterface_SUCCESS() {
        int output = circuitBreaker.execute(() -> 1+1);
        assertEquals(output, 2);
    }

    @Test
    public void functionalInterface_FailedOnce() {
        assertThrows(CircuitBreakerException.class, () -> {
            circuitBreaker.execute(() -> {
                throw new RuntimeException("Failed");
            });
        });
    }

    @Test
    public void functionalInterface_FailedRecover() {
        assertDoesNotThrow(() -> {
            circuitBreaker.execute(() -> {
                return 1+1;
            });
        });
    }

    public double convertGBPHKD (double gbp){
        return gbp/10;
    }

    @Test
    public void functionalInterfaceSupplier_SUCCESS() {
        Double output = assertDoesNotThrow(() -> {
            return circuitBreaker.execute(() -> convertGBPHKD(10.0));
        });
        assertEquals(1.0, output);
    }

    @Test
    public void functionalInterfaceSupplier_stateOPEN() {
        Double output = assertDoesNotThrow(() -> {
            return circuitBreaker.execute(() -> convertGBPHKD(10.0));
        });
        assertEquals(1.0, output);
    }

}