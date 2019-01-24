package com.unzip.andy.library.encode;

import java.io.IOException;
import java.nio.ByteBuffer;

public class FallbackZipEncoding implements ZipEncoding{

    private final String charset;

    public FallbackZipEncoding() {
        this.charset = null;
    }

    public FallbackZipEncoding(String charset) {
        this.charset = charset;
    }

    @Override
    public boolean canEncode(String name) {
        return true;
    }

    @Override
    public ByteBuffer encode(String name) throws IOException {
        return this.charset == null ? ByteBuffer.wrap(name.getBytes()) : ByteBuffer.wrap(name.getBytes(this.charset));
    }

    @Override
    public String decode(byte[] data) throws IOException {
        return this.charset == null ? new String(data) : new String(data, this.charset);
    }
}
