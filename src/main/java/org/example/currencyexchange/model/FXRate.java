package org.example.currencyexchange.model;

import java.math.BigDecimal;
import java.time.Instant;

public record FXRate(BigDecimal rate, Instant expireAt) {}
