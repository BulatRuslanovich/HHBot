package com.bipbup.utils;

import com.bipbup.config.HeadHunterProperties;
import com.bipbup.dto.Area;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AreaUtil {

    private final RestTemplate restTemplate;

    private final HeadHunterProperties properties;

    public Integer getAreaIdFromApi(String areaName) {
        if (areaName == null) return null;

        var areas = restTemplate.exchange(properties.areaGetUrl(), HttpMethod.GET, null,
                                          new ParameterizedTypeReference<List<Area>>() {}).getBody();

        return Optional.ofNullable(findAreaId(areas, areaName))
                    .map(Integer::valueOf)
                    .orElse(null);
    }

    private String findAreaId(List<Area> areas, String name) {
        Deque<List<Area>> stack = new LinkedList<>();
        stack.push(areas);

        while (!stack.isEmpty()) {
            var first = stack.pop();

            for (var area : first) {
                if (name.equalsIgnoreCase(area.name()))
                    return area.id();

                if (area.areas() != null)
                    stack.push(area.areas());
            }
        }

        return null;
    }
}
