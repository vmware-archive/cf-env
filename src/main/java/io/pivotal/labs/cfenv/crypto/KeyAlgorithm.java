package io.pivotal.labs.cfenv.crypto;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

public enum KeyAlgorithm {
    RSA("2a 86 48 86 f7 0d 01 01 01") {
        @Override
        public KeyFactory getFactory() {
            return RSAKeyFactory.INSTANCE;
        }

        @Override
        public KeySpec parsePkcs1PrivateKeySpec(byte[] keyBytes) throws IOException {
            return new PKCS8EncodedKeySpec(PKCS8.wrap(Arrays.asList(RSA.oid), keyBytes));
        }
    },
    EC("2a 86 48 ce 3d 02 01") {
        @Override
        public KeyFactory getFactory() {
            return ECKeyFactory.INSTANCE;
        }

        @Override
        public KeySpec parsePkcs1PrivateKeySpec(byte[] keyBytes) throws IOException {
            byte[] ecDomainParameters = DERInputStream.fromBytes(keyBytes, (in) -> {
                // as per RFC 5915
                in.readSequenceStart();
                in.readInteger(); // version
                in.readOctetString(); // private key
                in.readConstructedStart(DERTags.TAG_EC_PARAMETERS); // parameters
                return in.readObjectID();
            });
            return new PKCS8EncodedKeySpec(PKCS8.wrap(Arrays.asList(EC.oid, ecDomainParameters), keyBytes));
        }
    },
    DSA("2a 86 48 ce 38 04 01") {
        @Override
        public KeyFactory getFactory() {
            return DSAKeyFactory.INSTANCE;
        }

        @Override
        public KeySpec parsePkcs1PrivateKeySpec(byte[] keyBytes) throws IOException {
            return DERInputStream.fromBytes(keyBytes, in -> {
                in.readSequenceStart();
                in.readInteger();
                BigInteger prime = in.readBigInteger();
                BigInteger subprime = in.readBigInteger();
                BigInteger base = in.readBigInteger();
                in.readBigInteger();
                BigInteger privateKey = in.readBigInteger();
                return new DSAPrivateKeySpec(privateKey, prime, subprime, base);
            });
        }
    },
    DH("2a 86 48 86 f7 0d 01 03 01") {
        @Override
        public KeyFactory getFactory() {
            return DHKeyFactory.INSTANCE;
        }

        @Override
        public KeySpec parsePkcs1PrivateKeySpec(byte[] keyBytes) throws IOException {
            throw new IOException("there are no PKCS#1 DH keys");
        }
    };

    public static KeyAlgorithm determineAlgorithm(byte[] keyBytes) {
        if (contains(keyBytes, RSA.signature)) return RSA;
        else if (contains(keyBytes, EC.signature)) return EC;
        else if (contains(keyBytes, DSA.signature)) return DSA;
        else if (contains(keyBytes, DH.signature)) return DH;
        else return null;
    }

    private static boolean contains(byte[] haystack, byte[] needle) {
        bytes:
        for (int i = 0; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue bytes;
            }
            return true;
        }
        return false;
    }

    private final byte[] oid;
    private final byte[] signature;

    KeyAlgorithm(String oid) {
        this.oid = DatatypeConverter.parseHexBinary(oid.replace(" ", ""));
        this.signature = DEROutputStream.toBytes(out -> out.writeObjectID(this.oid));
    }

    public abstract KeyFactory getFactory();

    public abstract KeySpec parsePkcs1PrivateKeySpec(byte[] keyBytes) throws IOException;

}
