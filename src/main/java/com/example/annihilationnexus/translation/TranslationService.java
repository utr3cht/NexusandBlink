package com.example.annihilationnexus.translation;

import java.util.concurrent.CompletableFuture;

public interface TranslationService {
    CompletableFuture<String> translate(String text, String targetLang);
}
