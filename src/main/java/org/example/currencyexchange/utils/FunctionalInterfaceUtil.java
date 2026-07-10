package org.example.currencyexchange.utils;

@FunctionalInterface
public interface FunctionalInterfaceUtil<I,R> {
    R apply (I input) throws Exception;
}
