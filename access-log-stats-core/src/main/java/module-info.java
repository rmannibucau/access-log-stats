module com.github.rmannibucau.log.access.core {
    requires java.logging;

    opens com.github.rmannibucau.log.access.core;
    opens com.github.rmannibucau.log.access.core.cli;
    opens com.github.rmannibucau.log.access.core.io;
    opens com.github.rmannibucau.log.access.core.parser.api;
    opens com.github.rmannibucau.log.access.core.parser.impl;
    opens com.github.rmannibucau.log.access.core.parser.impl.generic;
    opens com.github.rmannibucau.log.access.core.parser.impl.generic.field;
    opens com.github.rmannibucau.log.access.core.service;
}
