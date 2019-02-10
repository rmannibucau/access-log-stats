package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;

/**
 * Reusable segment parsing a, IP (v6).
 */
public class IP6Parser extends GenericDataParser {
    private static final char[] HEXA_CHARS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f',
            'A', 'B', 'C', 'D', 'E', 'F'
    };
    private final boolean required;

    /**
     * @param required is this segment required or not.
     */
    public IP6Parser(final boolean required) {
        super(new DataParser[]{
                new CharParser(false, '['),
                new GluttonParser(true, "ipv6_part1", new char[]{':'}, HEXA_CHARS),
                new CharParser(true, ':'),
                new GluttonParser(true, "ipv6_part2", new char[]{':'}, HEXA_CHARS),
                new CharParser(true, ':'),
                new GluttonParser(true, "ipv6_part3", new char[]{':'}, HEXA_CHARS),
                new CharParser(true, ':'),
                new GluttonParser(true, "ipv6_part4", new char[]{':'}, HEXA_CHARS),
                new CharParser(true, ':'),
                new GluttonParser(true, "ipv6_part5", new char[]{':'}, HEXA_CHARS),
                new CharParser(true, ':'),
                new GluttonParser(true, "ipv6_part6", new char[]{':'}, HEXA_CHARS),
                new CharParser(true, ':'),
                new GluttonParser(true, "ipv6_part7", new char[]{':'}, HEXA_CHARS),
                new CharParser(true, ':'),
                new GluttonParser(true, "ipv6_part8", new char[]{']', ' '}, HEXA_CHARS),
                new CharParser(false, ']'),
                new GenericDataParser(new DataParser[] {
                        new CharParser(true, ':'),
                        new DigitsParser(true, "port", 5)
                }) {
                    @Override
                    public String name() {
                        return "port";
                    }

                    @Override
                    public boolean isRequired() {
                        return false;
                    }
                }
        });
        this.required = required;
    }

    @Override
    public String name() {
        return "ipv6";
    }

    @Override
    public boolean isRequired() {
        return required;
    }
}
