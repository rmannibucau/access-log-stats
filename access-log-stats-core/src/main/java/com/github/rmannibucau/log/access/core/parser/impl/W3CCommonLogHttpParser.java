package com.github.rmannibucau.log.access.core.parser.impl;

import com.github.rmannibucau.log.access.core.parser.impl.generic.DataParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.GenericParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.field.AuthUserParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.field.CharParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.field.DigitsParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.field.GluttonParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.field.IP4Parser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.field.IP6Parser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.field.OrParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.field.Rfc1123HostnameParser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.field.Rfc931Parser;
import com.github.rmannibucau.log.access.core.parser.impl.generic.field.WhitespaceParser;

/**
 * From the spec:
 * <pre>
 * The common logfile format is as follows:
 *     remotehost rfc931 authuser [date] "request" status bytes
 * remotehost
 * Remote hostname (or IP number if DNS hostname is not available, or if DNSLookup is Off.
 * rfc931
 * The remote logname of the user.
 * authuser
 * The username as which the user has authenticated himself.
 * [date]
 * Date and time of the request.
 * "request"
 * The request line exactly as it came from the client.
 * status
 * The HTTP status code returned to the client.
 * bytes
 * The content-length of the document transferred.
 * </pre>
 */
public class W3CCommonLogHttpParser extends GenericParser {
    public W3CCommonLogHttpParser() {
        super(new DataParser[]{
                new OrParser("remoteHost", true, new OrParser("remoteHost", true,
                        new Rfc1123HostnameParser(true), new IP4Parser(true)), new IP6Parser(true)),
                new WhitespaceParser(),
                new Rfc931Parser(true),
                new WhitespaceParser(),
                new AuthUserParser(true),
                new WhitespaceParser(),
                new CharParser(true, '['),
                // default date format is "%d/%b/%Y:%H:%M:%S %z" but no guarantee
                new GluttonParser(true, "date", new char[]{ ']' }, null), // no need to parse it yet
                new CharParser(true, ']'),
                new WhitespaceParser(),
                new CharParser(true, '"'),
                // "method url protocol", ex: "GET /foo HTTP/1.1" but comes from the client so can be ~anything actually
                new GluttonParser(true, "request", new char[]{ '"' }, null), // no need to parse it yet
                new CharParser(true, '"'),
                new WhitespaceParser(),
                new DigitsParser(true, "status", 3),
                new WhitespaceParser(),
                new DigitsParser(true, "bytes", 19) // max length of a long
        });
    }
}
