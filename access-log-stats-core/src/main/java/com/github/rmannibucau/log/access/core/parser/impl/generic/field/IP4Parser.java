package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;

/**
 * Reusable segment parsing a, IP.
 */
public class IP4Parser extends GenericDataParser {
    private final boolean required;

    /**
     * @param required is this segment required or not.
     */
    public IP4Parser(final boolean required) {
        super(new DataParser[]{
                new DigitsParser(true,"firstDigits", 3),
                new CharParser(true, '.'),
                new DigitsParser(true,"secondDigits", 3),
                new CharParser(true, '.'),
                new DigitsParser(true,"thirdDigits", 3),
                new CharParser(true, '.'),
                new DigitsParser(true, "lastDigits", 3)
        });
        this.required = required;
    }

    @Override
    public String name() {
        return "ip";
    }

    @Override
    public boolean isRequired() {
        return required;
    }
}
