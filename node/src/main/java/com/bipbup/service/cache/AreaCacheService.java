package com.bipbup.service.cache;

public interface AreaCacheService {

    Integer getAreaIdByName(String areaName);

    @SuppressWarnings("unused")
    void updateAreaIds();
}
