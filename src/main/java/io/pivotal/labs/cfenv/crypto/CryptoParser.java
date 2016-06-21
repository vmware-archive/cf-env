package io.pivotal.labs.cfenv.crypto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CryptoParser {

    private static final Pattern KEY_PATTERN = Pattern.compile("-----BEGIN ((?:(RSA|EC|DSA) )?(PUBLIC|PRIVATE)) KEY-----\n(.*)\n-----END \\1 KEY-----\n?", Pattern.DOTALL);

    public static Certificate parseCertificate(String certificateString) throws CertificateException {
        return X509CertificateFactory.INSTANCE.generateCertificate(toStream(certificateString));
    }

    private static ByteArrayInputStream toStream(String string) {
        return new ByteArrayInputStream(string.getBytes());
    }

    public static Key parseKey(String keyString) throws InvalidKeySpecException {
        Matcher matcher = KEY_PATTERN.matcher(keyString);
        if (!matcher.matches()) throw new InvalidKeySpecException("bad or unsupported PEM encoding: " + keyString);
        String algorithmString = matcher.group(2);
        String senseString = matcher.group(3);
        String bytesString = matcher.group(4);

        byte[] bytes = decodeBase64(bytesString);

        KeyFormat format = algorithmString == null ? KeyFormat.NATIVE : KeyFormat.LEGACY;

        KeyAlgorithm algorithm;
        try {
            algorithm = format.determineAlgorithm(algorithmString, bytes);
        } catch (IllegalArgumentException e) {
            throw new InvalidKeySpecException("unsupported algorithm: " + keyString, e);
        }

        KeySense sense = KeySense.valueOf(senseString);

        KeySpec spec;
        try {
            spec = format.parse(algorithm, sense, bytes);
        } catch (IOException e) {
            throw new InvalidKeySpecException("could not determine " + algorithmString + " key spec: " + keyString, e);
        }

        KeyFactory keyFactory = algorithm.getFactory();

        return sense.generate(keyFactory, spec);
    }

    private static byte[] decodeBase64(String keyBytesString) {
        return Base64.getMimeDecoder().decode(keyBytesString);
    }

}
