package org.example.currencyexchange.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Account {
    private int id;
    private String currency;
    private BigDecimal balance;

    public Account(int id, String currency, BigDecimal balance){
        this.id = id;
        this.currency = currency;
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);
    }

    public void withdraw(BigDecimal amount) throws IllegalArgumentException {
        if (amount.compareTo(BigDecimal.ZERO) < 0 || balance.subtract(amount).compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException();
        balance = balance.subtract(amount);
    }

    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException();
        balance = balance.add(amount);
    }

    public BigDecimal getBalance(){
        return balance;
    }

    public String getCurrency() {
        return currency;
    }
}
