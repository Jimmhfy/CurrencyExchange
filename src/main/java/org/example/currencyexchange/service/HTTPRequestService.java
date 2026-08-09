package org.example.currencyexchange.service;

import com.google.gson.Gson;
import org.example.currencyexchange.model.FXPair;
import org.example.currencyexchange.model.FXRate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class HTTPRequestService {
    private final String FX_ENDPOINT = "https://api.fxratesapi.com/latest";
    private final Duration expireDefault = Duration.ofMinutes(1);
    private HttpRequest request;
    private HttpClient client;

    public record HTTPResponse(String base, Map<String, BigDecimal> rates) {}

    public HTTPRequestService() throws URISyntaxException {
        this.request = HttpRequest.newBuilder().uri(new URI(FX_ENDPOINT)).GET().build();
        this.client = HttpClient.newBuilder().build();
    }

    public HashMap<FXPair, FXRate> requestRate() throws IOException, InterruptedException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Gson gson = new Gson();
        String json = response.body();
        HTTPResponse ratesResponse = gson.fromJson(json, HTTPResponse.class);
        HashMap<FXPair, FXRate> ret = new HashMap<>();
        for (Map.Entry<String, BigDecimal> e : ratesResponse.rates().entrySet()) {
            FXPair ratePair = new FXPair(ratesResponse.base, e.getKey());
            ret.put(ratePair, new FXRate(e.getValue(), Instant.now().plus(expireDefault)));
        }
        return ret;
    }
}
