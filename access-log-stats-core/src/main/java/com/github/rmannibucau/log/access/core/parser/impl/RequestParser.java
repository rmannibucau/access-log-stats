package com.github.rmannibucau.log.access.core.parser.impl;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.GenericParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.field.GluttonParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.field.WhitespaceParser;

public class RequestParser extends GenericParser {
    public RequestParser() {
        super(new DataParser[]{
                new GluttonParser(true, "method", new char[]{' '}, null),
                new WhitespaceParser(),
                new GluttonParser(true, "path", new char[]{' '}, null),
                new WhitespaceParser(),
                new GluttonParser(true, "protocol", new char[]{' '}, null)
        });
    }
}
