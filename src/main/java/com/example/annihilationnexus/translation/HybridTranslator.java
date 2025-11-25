package com.example.annihilationnexus.translation;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class HybridTranslator implements TranslationService {

    private final DeepLTranslator deepLTranslator;
    private final AzureTranslator azureTranslator;
    private final Logger logger;

    public HybridTranslator(DeepLTranslator deepLTranslator, AzureTranslator azureTranslator, Logger logger) {
        this.deepLTranslator = deepLTranslator;
        this.azureTranslator = azureTranslator;
        this.logger = logger;
    }

    @Override
    public CompletableFuture<String> translate(String text, String targetLang) {
        return deepLTranslator.translate(text, targetLang)
                .exceptionallyCompose(ex -> {
                    Throwable cause = ex.getCause();
                    if (cause instanceof DeepLTranslator.QuotaExceededException) {
                        logger.warning("DeepL quota exceeded. Falling back to Azure.");
                        return azureTranslator.translate(text, targetLang);
                    } else {
                        return CompletableFuture.failedFuture(ex);
                    }
                });
    }
}
