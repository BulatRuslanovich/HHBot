package com.bipbup.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@RequiredArgsConstructor
@RestController
public class WebHookController {
    private final UpdateProcessor processor;

    @PostMapping("/callback/update")
    public ResponseEntity<?> onUpdateReceived(@RequestBody Update update) {
        try {
            if (processor.processUpdate(update))
                return ResponseEntity.ok().build();
            else
                return ResponseEntity.badRequest().body("Failed to process the update.");
        } catch (Exception e) {
            log.error("Error processing update: {}", update, e);
            return ResponseEntity.internalServerError()
                    .body("Internal Server Error: " + e.getMessage());
        }
    }
}
