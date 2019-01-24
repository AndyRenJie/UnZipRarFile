package com.unzip.andy.library.encode;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;

public abstract class ZipEncodingHelper {

    private static final Map simpleEncodings = new HashMap();
    private static final byte[] HEX_DIGITS;
    public static final ZipEncoding UTF8_ZIP_ENCODING;

    ZipEncodingHelper() {
    }

    static ByteBuffer growBuffer(ByteBuffer b, int newCapacity) {
        b.limit(b.position());
        b.rewind();
        int c2 = b.capacity() * 2;
        ByteBuffer on = ByteBuffer.allocate(c2 < newCapacity ? newCapacity : c2);
        on.put(b);
        return on;
    }

    static void appendSurrogate(ByteBuffer bb, char c) {
        bb.put((byte) 37);
        bb.put((byte) 85);
        bb.put(HEX_DIGITS[c >> 12 & 15]);
        bb.put(HEX_DIGITS[c >> 8 & 15]);
        bb.put(HEX_DIGITS[c >> 4 & 15]);
        bb.put(HEX_DIGITS[c & 15]);
    }

    public static ZipEncoding getZipEncoding(String name) {
        if (isUTF8(name)) {
            return UTF8_ZIP_ENCODING;
        } else if (name == null) {
            return new FallbackZipEncoding();
        } else {
            ZipEncodingHelper.SimpleEncodingHolder h = (ZipEncodingHelper.SimpleEncodingHolder) simpleEncodings.get(name);
            if (h != null) {
                return h.getEncoding();
            } else {
                try {
                    Charset cs = Charset.forName(name);
                    return new NioZipEncoding(cs);
                } catch (UnsupportedCharsetException var3) {
                    return new FallbackZipEncoding(name);
                }
            }
        }
    }

    static boolean isUTF8(String encoding) {
        if (encoding == null) {
            encoding = System.getProperty("file.encoding");
        }

        return "UTF8".equalsIgnoreCase(encoding) || "utf-8".equalsIgnoreCase(encoding);
    }

    static {
        char[] cp437_high_chars = new char[]{'Ç', 'ü', 'é', 'â', 'ä', 'à', 'å', 'ç', 'ê', 'ë', 'è', 'ï', 'î', 'ì', 'Ä', 'Å', 'É', 'æ', 'Æ', 'ô', 'ö', 'ò', 'û', 'ù', 'ÿ', 'Ö', 'Ü', '¢', '£', '¥', '₧', 'ƒ', 'á', 'í', 'ó', 'ú', 'ñ', 'Ñ', 'ª', 'º', '¿', '⌐', '¬', '½', '¼', '¡', '«', '»', '░', '▒', '▓', '│', '┤', '╡', '╢', '╖', '╕', '╣', '║', '╗', '╝', '╜', '╛', '┐', '└', '┴', '┬', '├', '─', '┼', '╞', '╟', '╚', '╔', '╩', '╦', '╠', '═', '╬', '╧', '╨', '╤', '╥', '╙', '╘', '╒', '╓', '╫', '╪', '┘', '┌', '█', '▄', '▌', '▐', '▀', 'α', 'ß', 'Γ', 'π', 'Σ', 'σ', 'µ', 'τ', 'Φ', 'Θ', 'Ω', 'δ', '∞', 'φ', 'ε', '∩', '≡', '±', '≥', '≤', '⌠', '⌡', '÷', '≈', '°', '∙', '·', '√', 'ⁿ', '²', '■', ' '};
        ZipEncodingHelper.SimpleEncodingHolder cp437 = new ZipEncodingHelper.SimpleEncodingHolder(cp437_high_chars);
        simpleEncodings.put("CP437", cp437);
        simpleEncodings.put("Cp437", cp437);
        simpleEncodings.put("cp437", cp437);
        simpleEncodings.put("IBM437", cp437);
        simpleEncodings.put("ibm437", cp437);
        char[] cp850_high_chars = new char[]{'Ç', 'ü', 'é', 'â', 'ä', 'à', 'å', 'ç', 'ê', 'ë', 'è', 'ï', 'î', 'ì', 'Ä', 'Å', 'É', 'æ', 'Æ', 'ô', 'ö', 'ò', 'û', 'ù', 'ÿ', 'Ö', 'Ü', 'ø', '£', 'Ø', '×', 'ƒ', 'á', 'í', 'ó', 'ú', 'ñ', 'Ñ', 'ª', 'º', '¿', '®', '¬', '½', '¼', '¡', '«', '»', '░', '▒', '▓', '│', '┤', 'Á', 'Â', 'À', '©', '╣', '║', '╗', '╝', '¢', '¥', '┐', '└', '┴', '┬', '├', '─', '┼', 'ã', 'Ã', '╚', '╔', '╩', '╦', '╠', '═', '╬', '¤', 'ð', 'Ð', 'Ê', 'Ë', 'È', 'ı', 'Í', 'Î', 'Ï', '┘', '┌', '█', '▄', '¦', 'Ì', '▀', 'Ó', 'ß', 'Ô', 'Ò', 'õ', 'Õ', 'µ', 'þ', 'Þ', 'Ú', 'Û', 'Ù', 'ý', 'Ý', '¯', '´', '\u00ad', '±', '‗', '¾', '¶', '§', '÷', '¸', '°', '¨', '·', '¹', '³', '²', '■', ' '};
        ZipEncodingHelper.SimpleEncodingHolder cp850 = new ZipEncodingHelper.SimpleEncodingHolder(cp850_high_chars);
        simpleEncodings.put("CP850", cp850);
        simpleEncodings.put("Cp850", cp850);
        simpleEncodings.put("cp850", cp850);
        simpleEncodings.put("IBM850", cp850);
        simpleEncodings.put("ibm850", cp850);
        HEX_DIGITS = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};
        UTF8_ZIP_ENCODING = new FallbackZipEncoding("UTF8");
    }

    private static class SimpleEncodingHolder {
        private final char[] highChars;
        private Simple8BitZipEncoding encoding;

        SimpleEncodingHolder(char[] highChars) {
            this.highChars = highChars;
        }

        public synchronized Simple8BitZipEncoding getEncoding() {
            if (this.encoding == null) {
                this.encoding = new Simple8BitZipEncoding(this.highChars);
            }

            return this.encoding;
        }
    }
}
