package io.pivotal.labs.cfenv.crypto;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.stream.IntStream;

public class DEROutputStream extends FilterOutputStream {

    @FunctionalInterface
    public static interface DERWriter {
        public void write(DEROutputStream out) throws IOException;
    }

    public static byte[] toBytes(DERWriter writer) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            writer.write(new DEROutputStream(buffer));
        } catch (IOException e) {
            throw new AssertionError("implausible error writing DER in memory", e);
        }
        return buffer.toByteArray();
    }

    private static final int TAG_INTEGER = 0x02;
    private static final int TAG_OCTET_STRING = 0x04;
    private static final int TAG_NULL = 0x05;
    private static final int TAG_OBJECT_ID = 0x06;
    private static final int TAG_SEQUENCE = 0x30;

    public DEROutputStream(OutputStream out) {
        super(out);
    }

    public static int taggedLength(int length) {
        int lengthLength;
        if (length < 0) {
            throw new IllegalArgumentException();
        } else if (length < 128) {
            lengthLength = 1;
        } else if (length < 1 << 16) {
            lengthLength = 3;
        } else if (length < 1 << 24) {
            lengthLength = 4;
        } else {
            lengthLength = 5;
        }
        return 1 + lengthLength + length;
    }

    public static int measureInteger(int integer) {
        int bytes;
        if (integer < 1 << 8) {
            bytes = 1;
        } else if (integer < 1 << 16) {
            bytes = 2;
        } else if (integer < 1 << 24) {
            bytes = 3;
        } else {
            bytes = 4;
        }
        return taggedLength(bytes);
    }

    public void writeInteger(int integer) throws IOException {
        writeTagAndLength(TAG_INTEGER, 1);
        if (integer < 1 << 8) {
            write(integer);
        } else if (integer < 1 << 16) {
            writeBytes(sliceByte(integer, 1), sliceByte(integer, 0));
        } else if (integer < 1 << 24) {
            writeBytes(sliceByte(integer, 2), sliceByte(integer, 1), sliceByte(integer, 0));
        } else {
            writeBytes(sliceByte(integer, 3), sliceByte(integer, 2), sliceByte(integer, 1), sliceByte(integer, 0));
        }
    }

    private void writeBytes(byte... b) throws IOException {
        write(b);
    }

    public static int measureOctetString(byte[] octetString) {
        return taggedLength(octetString.length);
    }

    public void writeOctetString(byte[] octetString) throws IOException {
        writeTagAndLength(TAG_OCTET_STRING, octetString.length);
        write(octetString);
    }

    public static int measureNull() {
        return taggedLength(0);
    }

    public void writeNull() throws IOException {
        writeTagAndLength(TAG_NULL, 0);
    }

    public static int measureObjectID(byte[] oid) {
        if (oid == null) return measureNull();
        return taggedLength(oid.length);
    }

    public void writeObjectID(byte[] oid) throws IOException {
        if (oid == null) {
            writeNull();
        } else {
            writeTagAndLength(TAG_OBJECT_ID, oid.length);
            write(oid);
        }
    }

    public static int measureSequence(int... lengths) {
        return taggedLength(IntStream.of(lengths).sum());
    }

    public void writeSequenceStart(int... lengths) throws IOException {
        writeTagAndLength(TAG_SEQUENCE, IntStream.of(lengths).sum());
    }

    private void writeTagAndLength(int tag, int length) throws IOException {
        write(tag);
        writeLength(length);
    }

    private void writeLength(int length) throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException();
        } else if (length < 128) {
            writeShortLength(length);
        } else if (length < 1 << 16) {
            writeLongLength(sliceByte(length, 1), sliceByte(length, 0));
        } else if (length < 1 << 24) {
            writeLongLength(sliceByte(length, 2), sliceByte(length, 1), sliceByte(length, 0));
        } else {
            writeLongLength(sliceByte(length, 3), sliceByte(length, 2), sliceByte(length, 1), sliceByte(length, 0));
        }
    }

    private void writeShortLength(int length) throws IOException {
        write(length);
    }

    private void writeLongLength(byte... lengthBytes) throws IOException {
        write(0x80 | lengthBytes.length);
        write(lengthBytes);
    }

    private byte sliceByte(int integer, int index) {
        return (byte) (integer >> index * 8);
    }

}
