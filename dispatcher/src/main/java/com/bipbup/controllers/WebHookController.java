package com.bipbup.controllers;

import com.bipbup.exception.NullUpdateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@RequiredArgsConstructor
@RestController
public class WebHookController {

    private final UpdateProcessor processor;

    @PostMapping("/callback/update")
    public ResponseEntity<String> onUpdateReceived(@RequestBody Update update) {
        try {
            processor.processUpdate(update);
            return ResponseEntity.ok().build();
        } catch (NullUpdateException e) {
            log.error("Error processing update: {}", update, e);
            return ResponseEntity.badRequest().body("Failed to process the update.");
        }
    }
}
