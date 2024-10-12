package com.bipbup.service.cache;

public interface ConfigCacheService {

    @SuppressWarnings("UnusedReturnValue")
    Long putConfigId(long telegramId, long configId);

    Long getConfigId(long telegramId);

    void clearConfigId(long telegramId);
}
