package com.unzip.andy.library.encode;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Simple8BitZipEncoding implements ZipEncoding{

    private final char[] highChars;
    private final List reverseMapping;

    public Simple8BitZipEncoding(char[] highChars) {
        this.highChars = highChars;
        this.reverseMapping = new ArrayList(this.highChars.length);
        byte code = 127;

        for(int i = 0; i < this.highChars.length; ++i) {
            ++code;
            this.reverseMapping.add(new Simple8BitZipEncoding.Simple8BitChar(code, this.highChars[i]));
        }

        Collections.sort(this.reverseMapping);
    }

    public char decodeByte(byte b) {
        return b >= 0 ? (char)b : this.highChars[128 + b];
    }

    public boolean canEncodeChar(char c) {
        if (c >= 0 && c < 128) {
            return true;
        } else {
            Simple8BitZipEncoding.Simple8BitChar r = this.encodeHighChar(c);
            return r != null;
        }
    }

    public boolean pushEncodedChar(ByteBuffer bb, char c) {
        if (c >= 0 && c < 128) {
            bb.put((byte)c);
            return true;
        } else {
            Simple8BitZipEncoding.Simple8BitChar r = this.encodeHighChar(c);
            if (r == null) {
                return false;
            } else {
                bb.put(r.code);
                return true;
            }
        }
    }

    private Simple8BitZipEncoding.Simple8BitChar encodeHighChar(char c) {
        int i0 = 0;
        int i1 = this.reverseMapping.size();

        while(i1 > i0) {
            int i = i0 + (i1 - i0) / 2;
            Simple8BitZipEncoding.Simple8BitChar m = (Simple8BitZipEncoding.Simple8BitChar)this.reverseMapping.get(i);
            if (m.unicode == c) {
                return m;
            }

            if (m.unicode < c) {
                i0 = i + 1;
            } else {
                i1 = i;
            }
        }

        if (i0 >= this.reverseMapping.size()) {
            return null;
        } else {
            Simple8BitZipEncoding.Simple8BitChar r = (Simple8BitZipEncoding.Simple8BitChar)this.reverseMapping.get(i0);
            if (r.unicode != c) {
                return null;
            } else {
                return r;
            }
        }
    }

    @Override
    public boolean canEncode(String name) {
        for(int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (!this.canEncodeChar(c)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ByteBuffer encode(String name) {
        ByteBuffer out = ByteBuffer.allocate(name.length() + 6 + (name.length() + 1) / 2);

        for(int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (out.remaining() < 6) {
                out = ZipEncodingHelper.growBuffer(out, out.position() + 6);
            }

            if (!this.pushEncodedChar(out, c)) {
                ZipEncodingHelper.appendSurrogate(out, c);
            }
        }

        out.limit(out.position());
        out.rewind();
        return out;
    }

    @Override
    public String decode(byte[] data) throws IOException {
        char[] ret = new char[data.length];

        for(int i = 0; i < data.length; ++i) {
            ret[i] = this.decodeByte(data[i]);
        }

        return new String(ret);
    }

    private static final class Simple8BitChar implements Comparable {
        public final char unicode;
        public final byte code;

        Simple8BitChar(byte code, char unicode) {
            this.code = code;
            this.unicode = unicode;
        }

        @Override
        public int compareTo(Object o) {
            Simple8BitZipEncoding.Simple8BitChar a = (Simple8BitZipEncoding.Simple8BitChar)o;
            return this.unicode - a.unicode;
        }

        @Override
        public String toString() {
            return "0x" + Integer.toHexString('\uffff' & this.unicode) + "->0x" + Integer.toHexString(255 & this.code);
        }
    }
}
