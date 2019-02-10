package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

public class AuthUserParser extends GluttonParser {
    public AuthUserParser(final boolean required) {
        super(required, "authuser", new char[] { ' ' }, null);
    }
}
