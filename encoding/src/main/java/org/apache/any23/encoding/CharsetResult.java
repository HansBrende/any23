package org.apache.any23.encoding;

import java.nio.charset.StandardCharsets;

class CharsetResult extends Error {

    final String name;

    private CharsetResult(String name) {
        super(null, null, false, false);
        this.name = name;
    }

    static final CharsetResult UTF_8 = new CharsetResult(StandardCharsets.UTF_8.name());

}
