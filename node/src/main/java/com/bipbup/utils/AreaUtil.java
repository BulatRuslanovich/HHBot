package com.bipbup.utils;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@Slf4j
@Component
public class AreaUtil {
    private AreaUtil() {
    }

    private static final String URL = "https://api.hh.ru/areas";
    private static final int HTTP_STATUS_OK = 200;

    public static String getAreaIdByName(final String areaName) {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .build();
        HttpResponse<String> response;
        try {
            response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            log.error("Error querying API", e);
            return null;
        }

        if (response.statusCode() == HTTP_STATUS_OK) {
            JSONArray areas = new JSONArray(response.body());
            return findAreaId(areas, areaName);
        } else {
            log.error("Error querying API {}: {} {}",
                    response.request().method(),
                    response.request().uri(),
                    response.statusCode());

            return null;
        }
    }

    private static String findAreaId(final JSONArray areas,
                                     final String name) {
        for (int i = 0; i < areas.length(); i++) {
            JSONObject area = areas.getJSONObject(i);

            if (area.getString("name").equalsIgnoreCase(name)) {
                return area.getString("id");
            }

            if (area.has("areas")) {
                String foundAreaId =
                        findAreaId(area.getJSONArray("areas"), name);
                if (foundAreaId != null) {
                    return foundAreaId;
                }
            }
        }
        return null;
    }
}
