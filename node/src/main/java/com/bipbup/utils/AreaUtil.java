package com.bipbup.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import java.util.Deque;
import java.util.LinkedList;
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
            var areas = objectMapper.readTree(response.body());

            return Optional.ofNullable(findAreaId(areas, areaName))
                    .map(Integer::valueOf)
                    .orElse(null);
        }

        log.error("Failed to retrieve areas: {} {}", response.statusCode(), response.body());
        return null;
    }

    private String findAreaId(JsonNode areas, String name) {
        Deque<JsonNode> stack = new LinkedList<>();
        stack.push(areas);

        while (!stack.isEmpty()) {
            var first = stack.pop();

            for (var area : first) {
                var areaName = area.get("name").asText();
                if (name.equalsIgnoreCase(areaName))
                    return area.get("id").asText();

                Optional.ofNullable(area.get("areas"))
                        .ifPresent(stack::push);
            }
        }

        return null;
    }
}
