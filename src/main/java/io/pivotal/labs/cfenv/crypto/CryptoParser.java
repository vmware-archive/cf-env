package io.pivotal.labs.cfenv.crypto;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CryptoParser {

    private static final Pattern KEY_PATTERN = Pattern.compile("-----BEGIN ((?:(RSA|EC|DSA) )?(PUBLIC|PRIVATE)) KEY-----\n(.*)\n-----END \\1 KEY-----\n?", Pattern.DOTALL);

    private static final byte[] RSA_OID = bytes("2a 86 48 86 f7 0d 01 01 01");
    private static final byte[] EC_OID = bytes("2a 86 48 ce 3d 02 01");
    private static final byte[] DSA_OID = bytes("2a 86 48 ce 38 04 01");
    private static final byte[] DH_OID = bytes("2a 86 48 86 f7 0d 01 03 01");

    private static byte[] bytes(String hex) {
        return DatatypeConverter.parseHexBinary(hex.replace(" ", ""));
    }

    private static final byte[] RSA_SIGNATURE = signature(RSA_OID);
    private static final byte[] EC_SIGNATURE = signature(EC_OID);
    private static final byte[] DSA_SIGNATURE = signature(DSA_OID);
    private static final byte[] DH_SIGNATURE = signature(DH_OID);

    private static byte[] signature(byte[] oid) {
        return DEROutputStream.toBytes(out -> out.writeObjectID(oid));
    }

    public static Certificate parseCertificate(String certificateString) throws CertificateException {
        return X509CertificateFactory.INSTANCE.generateCertificate(toStream(certificateString));
    }

    private static ByteArrayInputStream toStream(String string) {
        return new ByteArrayInputStream(string.getBytes());
    }

    public static Key parseKey(String keyString) throws InvalidKeySpecException {
        Matcher matcher = KEY_PATTERN.matcher(keyString);
        if (!matcher.matches()) throw new IllegalArgumentException("bad or unsupported PEM encoding: " + keyString);
        String algorithm = matcher.group(2);
        String keyType = matcher.group(3);
        String rawKeyBytesString = matcher.group(4);

        byte[] rawKeyBytes = decodeBase64(rawKeyBytesString);

        boolean publicKey;
        switch (keyType) {
            case "PUBLIC":
                publicKey = true;
                break;
            case "PRIVATE":
                publicKey = false;
                break;
            default:
                throw new IllegalArgumentException("unsupported key type: " + keyString);
        }

        KeyFactory keyFactory;
        KeySpec spec;
        if (algorithm == null) {
            keyFactory = chooseKeyFactory(rawKeyBytes);
            if (publicKey) spec = new X509EncodedKeySpec(rawKeyBytes);
            else spec = new PKCS8EncodedKeySpec(rawKeyBytes);
        } else {
            try {
                switch (algorithm) {
                    case "RSA":
                        keyFactory = RSAKeyFactory.INSTANCE;
                        spec = rsaKeySpec(rawKeyBytes);
                        break;
                    case "EC":
                        keyFactory = ECKeyFactory.INSTANCE;
                        spec = ecKeySpec(rawKeyBytes);
                        break;
                    case "DSA":
                        keyFactory = DSAKeyFactory.INSTANCE;
                        spec = dsaKeySpec(rawKeyBytes);
                        break;
                    default:
                        throw new IllegalArgumentException("unsupported algorithm: " + algorithm);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("could not determine " + algorithm + " key spec: " + keyString, e);
            }
        }

        if (keyFactory == null) throw new IllegalArgumentException("unsupported algorithm: " + keyString);

        if (publicKey) return keyFactory.generatePublic(spec);
        else return keyFactory.generatePrivate(spec);
    }

    private static byte[] decodeBase64(String keyBytesString) {
        return Base64.getMimeDecoder().decode(keyBytesString);
    }

    private static KeyFactory chooseKeyFactory(byte[] keyBytes) {
        if (contains(keyBytes, RSA_SIGNATURE)) return RSAKeyFactory.INSTANCE;
        else if (contains(keyBytes, EC_SIGNATURE)) return ECKeyFactory.INSTANCE;
        else if (contains(keyBytes, DSA_SIGNATURE)) return DSAKeyFactory.INSTANCE;
        else if (contains(keyBytes, DH_SIGNATURE)) return DHKeyFactory.INSTANCE;
        else return null;
    }

    private static KeySpec rsaKeySpec(byte[] pkcs1KeyBytes) {
        return new PKCS8EncodedKeySpec(PKCS8.wrap(Arrays.asList(RSA_OID), pkcs1KeyBytes));
    }

    private static KeySpec ecKeySpec(byte[] pkcs1KeyBytes) throws IOException {
        byte[] ecDomainParameters = DERInputStream.fromBytes(pkcs1KeyBytes, (in) -> {
            // as per RFC 5915
            in.readSequenceStart();
            in.readInteger(); // version
            in.readOctetString(); // private key
            in.readConstructedStart(DERTags.TAG_EC_PARAMETERS); // parameters
            return in.readObjectID();
        });
        return new PKCS8EncodedKeySpec(PKCS8.wrap(Arrays.asList(EC_OID, ecDomainParameters), pkcs1KeyBytes));
    }

    private static KeySpec dsaKeySpec(byte[] rawKeyBytes) throws IOException {
        return DERInputStream.fromBytes(rawKeyBytes, in -> {
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

}
