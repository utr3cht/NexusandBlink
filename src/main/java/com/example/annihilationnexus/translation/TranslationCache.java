package com.example.annihilationnexus.translation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;

public class TranslationCache {

    private final Cache<String, String> cache;

    public TranslationCache() {
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build();
    }

    public String get(String text, String targetLang) {
        return cache.getIfPresent(generateKey(text, targetLang));
    }

    public void put(String text, String targetLang, String translation) {
        cache.put(generateKey(text, targetLang), translation);
    }

    private String generateKey(String text, String targetLang) {
        return targetLang + ":" + text;
    }
}
