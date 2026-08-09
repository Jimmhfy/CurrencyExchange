package org.example.currencyexchange.service;

import org.example.currencyexchange.model.Account;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class MoneyTransferService {
    CurrencyFXService currencyFXService;

    public MoneyTransferService(CurrencyFXService currencyFXService) {
        this.currencyFXService = currencyFXService;
    }

    public void transfer(Account from, Account to, BigDecimal amount) throws IOException, InterruptedException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException();

        from.withdraw(amount);
        if (!from.getCurrency().equals(to.getCurrency())) {
            BigDecimal rate = this.currencyFXService.getRates(from.getCurrency(), to.getCurrency());
            amount = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
        }
        to.deposit(amount);
    }
}
