package com.github.rmannibucau.log.access.core.parser.api;

import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;

public class Line {
    private final Map<String, String> data;
    private final boolean valid;

    private Line(final Map<String, String> data, final boolean success) {
        this.data = requireNonNull(data, "You can't create a line without data");
        this.valid = success;
    }

    public boolean isValid() {
        return valid;
    }

    public Map<String, String> getData() {
        return unmodifiableMap(data);
    }

    /**
     * Create a successful parsed line.
     *
     * @param fields the data representing the line.
     * @return the line initialized with the fields passed as parameters.
     */
    public static Line of(final Map<String, String> fields) {
        return new Line(fields, true);
    }

    /**
     * Create a failed parsing initialized with a single field <pre>data</pre> containing the incoming line
     * and <pre>error</pre> containing the cause of the failure.
     *
     * @param input the data which failed to be parsed.
     * @param cause a message explaning why parsing failed.
     * @return the failed line.
     */
    public static Line failed(final String input, final String cause) {
        final Map<String, String> data = new HashMap<>();
        data.put("data", input);
        data.put("error", cause);
        return new Line(data, false);
    }
}
