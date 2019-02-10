package com.github.rmannibucau.log.access.core.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class LineReader {
    private static final int DEFAULT_LENGTH = 512;

    private final Path path;
    private final Charset charset;
    private final Consumer<String> onLine;
    private final AtomicBoolean done = new AtomicBoolean();
    private final Consumer<Long> onPositionChange;
    private final ByteBuffer readBuffer;

    private long startOffset;

    public LineReader(final Path path, final Charset charset,
                      final long startOffset,
                      final Consumer<String> onNewLine,
                      final Consumer<Long> onPositionChange) {
        this.path = path;
        this.charset = charset;
        this.startOffset = startOffset;
        this.onLine = onNewLine;
        this.onPositionChange = onPositionChange;
        this.readBuffer = ByteBuffer.allocate(DEFAULT_LENGTH);
    }

    // will not read a line not ending with EOL to avoid to read partial lines
    // todo: detect it is a new file (log rotation) and just restart from 0
    public void iterate() throws IOException {
        if (done.get()) {
            return;
        }

        final CharsetDecoder decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        try (final SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.READ)) {
            if (startOffset > 0) {
                channel.position(startOffset);
            }


            byte[] buffer = new byte[0];
            while (!done.get()) {
                // find eol
                int bufferIdx = 0;
                int delimiter = -1;
                boolean eof = false;
                while (!done.get()) {
                    // get data to analyze
                    final int neededBufferSize = bufferIdx + 1;
                    while (!done.get() && buffer.length <= neededBufferSize && !eof) {
                        eof = channel.read(readBuffer) == -1;
                        readBuffer.flip();
                        final byte[] newBuffer = new byte[buffer.length + readBuffer.limit()];
                        System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
                        System.arraycopy(readBuffer.array(), 0, newBuffer, buffer.length, readBuffer.limit());
                        buffer = newBuffer;
                        readBuffer.clear();
                    }

                    if (eof && buffer.length == 0) {
                        break;
                    }

                    final byte currentByte = buffer[bufferIdx];
                    if (currentByte == '\n'|| currentByte == '\r') {
                        delimiter = bufferIdx;
                        break;
                    }

                    bufferIdx += 1;
                }

                // nothing anymore, quit and ignore last part of the buffer cause it can be a partial line
                if (delimiter <= 0) {
                    if (eof) {
                        break;
                    }
                    continue;
                }

                // add line and update offset
                final String decoded = decoder.decode(ByteBuffer.wrap(Arrays.copyOfRange(buffer, 0, delimiter))).toString();
                onLine.accept(decoded);
                buffer = Arrays.copyOfRange(buffer, delimiter + 1, buffer.length);
                startOffset += delimiter + 1;
                onPositionChange.accept(startOffset);
            }
        }
    }

    public void cancel() {
        done.compareAndSet(false, true);
    }
}
