package com.example.annihilationnexus.translation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AzureTranslator implements TranslationService {

    private final String apiKey;
    private final String region;
    private final HttpClient httpClient;
    private final Gson gson;
    private static final String API_URL = "https://api.cognitive.microsofttranslator.com/translate?api-version=3.0";

    public AzureTranslator(String apiKey, String region) {
        this.apiKey = apiKey;
        this.region = region;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    @Override
    public CompletableFuture<String> translate(String text, String targetLang) {
        if (apiKey == null || apiKey.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Azure API key is missing"));
        }

        String url = API_URL + "&to=" + targetLang;
        JsonArray bodyArray = new JsonArray();
        JsonObject textObj = new JsonObject();
        textObj.addProperty("text", text);
        bodyArray.add(textObj);
        String jsonBody = gson.toJson(bodyArray);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Ocp-Apim-Subscription-Key", apiKey)
                .header("Ocp-Apim-Subscription-Region", region)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        throw new CompletionException(new RuntimeException(
                                "Azure API Error: " + response.statusCode() + " " + response.body()));
                    }

                    JsonArray json = gson.fromJson(response.body(), JsonArray.class);
                    if (json != null && json.size() > 0) {
                        JsonArray translations = json.get(0).getAsJsonObject().getAsJsonArray("translations");
                        if (translations != null && translations.size() > 0) {
                            return translations.get(0).getAsJsonObject().get("text").getAsString();
                        }
                    }
                    return text;
                });
    }
}
