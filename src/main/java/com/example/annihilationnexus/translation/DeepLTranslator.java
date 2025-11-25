package com.example.annihilationnexus.translation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class DeepLTranslator implements TranslationService {

    private final String apiKey;
    private final HttpClient httpClient;
    private final Gson gson;
    private static final String API_URL = "https://api-free.deepl.com/v2/translate";

    public DeepLTranslator(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    @Override
    public CompletableFuture<String> translate(String text, String targetLang) {
        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("DeepL API key is missing"));
        }

        String params = "auth_key=" + apiKey + "&text=" + java.net.URLEncoder.encode(text, StandardCharsets.UTF_8)
                + "&target_lang=" + targetLang;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(params))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 456) {
                        throw new CompletionException(new QuotaExceededException("DeepL Quota Exceeded"));
                    }
                    if (response.statusCode() != 200) {
                        throw new CompletionException(new RuntimeException(
                                "DeepL API Error: " + response.statusCode() + " " + response.body()));
                    }

                    JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                    JsonArray translations = json.getAsJsonArray("translations");
                    if (translations != null && translations.size() > 0) {
                        return translations.get(0).getAsJsonObject().get("text").getAsString();
                    }
                    return text;
                });
    }

    public static class QuotaExceededException extends RuntimeException {
        public QuotaExceededException(String message) {
            super(message);
        }
    }
}
