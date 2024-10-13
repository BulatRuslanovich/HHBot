package com.bipbup.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AreaUtil {

    private static final String URL = "https://api.hh.ru/areas";

    private final HttpClient client;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public Integer getAreaIdFromApi(String areaName) {
        if (areaName == null) return null;

        var request = HttpRequest.newBuilder().uri(URI.create(URL)).build();
        var response = client.send(request, ofString());

        if (response.statusCode() == HttpStatus.OK.value()) {
            var areas = objectMapper.readValue(response.body(), new TypeReference<List<Map<String, Object>>>() {});
            return Optional.ofNullable(findAreaId(areas, areaName))
                    .map(Integer::valueOf)
                    .orElse(null);
        }

        log.error("Failed to retrieve areas: {} {}", response.statusCode(), response.body());
        return null;
    }

    private String findAreaId(List<Map<String, Object>> areas, String name) {
        Deque<List<Map<String, Object>>> stack = new LinkedList<>();
        stack.push(areas);

        while (!stack.isEmpty()) {
            var first = stack.pop();
            if (first == null) continue;

            for (var area : first) {
                String areaName = (String) area.get("name");
                if (name.equalsIgnoreCase(areaName))
                    return (String) area.get("id");

                Optional.ofNullable(area.get("areas"))
                        .filter(List.class::isInstance)
                        .map(List.class::cast)
                        .ifPresent(stack::push);
            }
        }

        return null;
    }
}
