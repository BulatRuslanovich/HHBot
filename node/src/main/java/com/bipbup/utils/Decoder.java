package com.bipbup.utils;

import lombok.RequiredArgsConstructor;
import org.hashids.Hashids;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class Decoder {
    private final Hashids hashids;

    public Long idOf(String hash) {
        var decode = hashids.decode(hash);

        if (decode != null && decode.length > 0) {
            return decode[0];
        }

        return 0L;
    }
}
