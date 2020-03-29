package org.apache.any23.encoding;

import org.jsoup.nodes.Attribute;
import org.rypt.f8.Utf8Statistics;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.CharBuffer;
import java.util.Arrays;

class EncodingStatsReader extends Reader {

    public static void printTokens(String string) throws Exception {
        Field field = Attribute.class.getDeclaredField("val");
        field.setAccessible(true);
        ParseErrorList errors = ParseErrorList.tracking(Integer.MAX_VALUE);
        Tokeniser tk = new Tokeniser(new CharacterReader(new StringReader(string)), errors);
        Token t;
        while (!(t = tk.read()).isEOF()) {
            System.out.println(t.type + ": " + t);
            if (t.isStartTag()) {
                t.asStartTag().getAttributes().asList().forEach(attr -> {
                    try {
                        System.out.println("    '" + attr.getKey() + "'='" + attr.getValue() + "'" + "; " + field.get(attr) + " " + attr.getClass());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        System.out.println(errors);
    }

    public static void main(String[] args) throws Exception {
        printTokens("he mentioned that 2<five and also hello there='' how are you? I'm fine wasn't and 2 > 1");
    }

    private final InputStream in;
    private final boolean markSupported;

    private int maxBufLen;
    private byte[] buf;
    private long pos;
    private long maxPos;
    private long mark;

    EncodingStatsReader(InputStream in) {
        this.in = in;
        this.markSupported = in.markSupported();
    }

//    @Override
//    public int read() throws IOException {
//        return in.read();
//    }

    Utf8Statistics stats = new Utf8Statistics();

    private int maxChunkLen = 65536;

    ByteArrayOutputStream lastChunk = new ByteArrayOutputStream();

    private void processChunk() {
        // TODO: process UTF-16, etc. here via icu4j, and all other charsets that don't rely on lang
        //System.out.println("PROCESSING CHUNK: " + lastChunk.size());
    }

    private void handleStats(byte[] b, int offset, int len) {
        //System.out.println("handling stats with len " + len);
        stats.write(b, offset, len); // TODO implement custom handler that looks for unique codepoints
        //System.out.println(stats + " looks like utf8: " + stats.looksLikeUtf8());
//        if (stats.looksLikeUtf8()) {
//            throw CharsetResult.UTF_8;
//        }

        int availableLen = maxChunkLen - lastChunk.size();
        lastChunk.write(b, offset, Math.min(len, availableLen));

        if (lastChunk.size() == maxChunkLen) {
            processChunk();
            lastChunk.reset();
            int leftToWrite = len - availableLen;
            if (leftToWrite > 0) {
                lastChunk.write(b, offset + availableLen, leftToWrite);
            }
        }

    }

    private void handleEOF() {
        stats.close();
        processChunk(); // TODO what if there are only a couple bytes left in chunk? Ignore? Make prev chunk bigger? Or... read twice as much, divide remaining buffer by 2?
        lastChunk.reset();
    }

//    @Override
//    public long skip(long n) throws IOException {
//        return in.skip(n);
//    }

    @Override
    public boolean markSupported() {
        return markSupported;
    }

    @Override
    public void mark(int readAheadLimit) throws IOException {
        in.mark(readAheadLimit);
        mark = pos;
    }

    @Override
    public void reset() throws IOException {
        in.reset();
        pos = mark;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        final byte[] buf = len > maxBufLen ? this.buf = new byte[maxBufLen = len] : this.buf;
        int count = in.read(buf, 0, len);
        if (count < 0) {
            if (pos != -1) {
                handleEOF();
                pos = -1;
            }
            return -1;
        }
        pos += count;
        int countNew = (int)(pos - maxPos);
        if (countNew > 0) {
            handleStats(buf, count - countNew, countNew);
            maxPos = pos;
        }
        for (int i = 0; i < count; i++) {
            cbuf[off + i] = (char)(buf[i] & 0xff);
        }
        return count;
    }

    @Override
    public void close() throws IOException {
        buf = null;
        maxBufLen = 0;
        in.close();
    }
}
