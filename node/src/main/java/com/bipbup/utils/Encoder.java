package com.bipbup.utils;

import lombok.RequiredArgsConstructor;
import org.hashids.Hashids;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class Encoder {

    private final Hashids hashids;

    public String hashOf(long number) {
        var encode = hashids.encode(number);

        if (encode != null && !encode.isEmpty())
            return encode;

        return "";
    }
}
