package io.pivotal.labs.cfenv.crypto;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CryptoParser {

    private static final Pattern KEY_PATTERN = Pattern.compile("-----BEGIN (PUBLIC|PRIVATE) KEY-----\n(.*)\n-----END \\1 KEY-----\n?", Pattern.DOTALL);

    private static final byte[] RSA_SIGNATURE = bytes("06 09 2a 86 48 86 f7 0d 01 01 01");
    private static final byte[] EC_SIGNATURE = bytes("06 07 2a 86 48 ce 3d 02 01");
    private static final byte[] DSA_SIGNATURE = bytes("06 07 2a 86 48 ce 38 04 01");
    private static final byte[] DH_SIGNATURE = bytes("06 09 2a 86 48 86 f7 0d 01 03 01");

    private static byte[] bytes(String hex) {
        return DatatypeConverter.parseHexBinary(hex.replace(" ", ""));
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
        String keyBytesString = matcher.group(2);

        byte[] keyBytes = Base64.getMimeDecoder().decode(keyBytesString);

        KeyFactory keyFactory = chooseKeyFactory(keyBytes);
        if (keyFactory == null) throw new IllegalArgumentException("unsupported algorithm: " + keyString);

        String keyType = matcher.group(1);
        switch (keyType) {
            case "PUBLIC":
                return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
            case "PRIVATE":
                return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
            default:
                throw new IllegalArgumentException("unsupported key type: " + keyString);
        }
    }

    private static KeyFactory chooseKeyFactory(byte[] keyBytes) {
        if (contains(keyBytes, RSA_SIGNATURE)) return RSAKeyFactory.INSTANCE;
        else if (contains(keyBytes, EC_SIGNATURE)) return ECKeyFactory.INSTANCE;
        else if (contains(keyBytes, DSA_SIGNATURE)) return DSAKeyFactory.INSTANCE;
        else if (contains(keyBytes, DH_SIGNATURE)) return DHKeyFactory.INSTANCE;
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

}
