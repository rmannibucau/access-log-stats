package com.github.rmannibucau.log.access.core.service;

import java.util.logging.Logger;

import com.github.rmannibucau.log.access.core.Launcher;

public class Persistence {
    private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());

    public void onNewOffset(final String file, final long newOffset) {
        // for now just log it to let a user recover manually
        LOGGER.info(() -> "Progression > file='" + file + "', offset=" + newOffset);
    }
}
