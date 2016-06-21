package io.pivotal.labs.cfenv.crypto;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class DERInputStream extends PushbackInputStream {

    @FunctionalInterface
    public static interface DERReader<T> {
        public T read(DERInputStream in) throws IOException;
    }

    static <T> T fromBytes(byte[] bytes, DERReader<T> reader) throws IOException {
        return reader.read(new DERInputStream(new ByteArrayInputStream(bytes)));
    }

    public DERInputStream(InputStream in) {
        super(in);
    }

    public int readSequenceStart() throws IOException {
        readTag(DERTags.TAG_SEQUENCE);
        return readLength();
    }

    public int readInteger() throws IOException {
        readTag(DERTags.TAG_INTEGER);
        int length = readLength();
        switch (length) {
            case 1:
                return readByte();
            case 2:
                return readByte() << 8 + readByte();
            case 3:
                return readByte() << 16 + readByte() << 8 + readByte();
            case 4:
                return readByte() << 24 + readByte() << 16 + readByte() << 8 + readByte();
            default:
                throw new IOException("length too long: " + length);
        }
    }

    public byte[] readOctetString() throws IOException {
        readTag(DERTags.TAG_OCTET_STRING);
        int length = readLength();
        return readFully(length);
    }

    public int readConstructedStart(int expectedTag) throws IOException {
        readTag(expectedTag);
        return readLength();
    }

    public byte[] readObjectID() throws IOException {
        readTag(DERTags.TAG_OBJECT_ID);
        int length = readLength();
        return readFully(length);
    }

    private void readTag(int expectedTag) throws IOException {
        int tag = readByte();
        if (tag != expectedTag) {
            unread(tag);
            throw new IOException(String.format("expected tag %02x but got %02x", expectedTag, tag));
        }
    }

    private int readLength() throws IOException {
        int lengthStart = readByte();
        if (lengthStart < 128) {
            return lengthStart;
        } else {
            int count = lengthStart & 0x7f;
            switch (count) {
                case 1:
                    return readByte();
                case 2:
                    return readByte() << 8 + readByte();
                case 3:
                    return readByte() << 16 + readByte() << 8 + readByte();
                case 4:
                    return readByte() << 24 + readByte() << 16 + readByte() << 8 + readByte();
                default:
                    throw new IOException("long length too short or long: " + count);
            }
        }
    }

    private int readByte() throws IOException {
        int b = in.read();
        if (b == -1) throw new EOFException();
        return b;
    }

    private byte[] readFully(int length) throws IOException {
        byte[] bytes = new byte[length];

        int totalRead = 0;
        while (totalRead < length) {
            int read = in.read(bytes, totalRead, bytes.length - totalRead);
            if (read == -1) throw new EOFException();
            totalRead += read;
        }

        return bytes;
    }

}
