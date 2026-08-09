package org.example.currencyexchange.controller;

import org.example.currencyexchange.task.RateFetchTask;
import org.example.currencyexchange.service.CurrencyFXService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.math.BigDecimal;


@Controller
public class RestAPIController {
    private final CurrencyFXService currencyFXService;
    public RestAPIController(CurrencyFXService currencyFXService) {
        this.currencyFXService = currencyFXService;
    }

//    @GetMapping("/convert/{currency}/{input}")
//    public ResponseEntity<String> convertCurrency(@PathVariable String currency, @PathVariable BigDecimal input){
//        this.currencyFXService.submitRateCalculationTask(new RateFetchTask());
//        return new ResponseEntity<>(output, HttpStatus.OK);
//    }

    @GetMapping({"/rates/{from}/{to}", "/rates/{from}/{to}/{amount}"})
    public ResponseEntity<String> getRates(@PathVariable String from, @PathVariable String to, @PathVariable(required = false) BigDecimal amount) {
        try {
            BigDecimal rate = this.currencyFXService.getRates(from, to);
            if (amount != null)
                rate = rate.multiply(amount);
            return new ResponseEntity<>(rate.toString(), HttpStatus.OK);
        } catch (IOException|InterruptedException e) {
            return new ResponseEntity<>("0.0", HttpStatus.BAD_REQUEST);
        }
    }
}
