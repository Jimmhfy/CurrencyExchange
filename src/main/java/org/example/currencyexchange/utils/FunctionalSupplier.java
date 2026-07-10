package org.example.currencyexchange.utils;

@FunctionalInterface
public interface FunctionalSupplier<R> {
    R apply() throws Exception;
}
