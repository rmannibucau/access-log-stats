package com.github.rmannibucau.log.access.core.parser.impl.generic.field;

/**
 * Implements a parser for user respecting RFC931:
 *
 * <pre>
 *      USERID
 *
 *       In this case, <additional-info> is a string consisting of an
 *       operating system name, followed by a ":", followed by user
 *       identification string in a format peculiar to the operating system
 *       indicated.  Permitted operating system names are specified in
 *       RFC-923, "Assigned Numbers" or its successors.  The only other
 *       names permitted are "TAC" to specify a BBN Terminal Access
 *       Controller, and "OTHER" to specify any other operating system not
 *       yet registered with the NIC.
 * </pre>
 *
 * Since we don't need that field more than that we just use a glouton impl and don't parse the subvalue.
 */
public class Rfc931Parser extends GluttonParser {
    public Rfc931Parser(final boolean required) {
        super(required, "rfc931", new char[] { ' ' }, null);
    }
}
