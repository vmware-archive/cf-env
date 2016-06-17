package io.pivotal.labs.cfenv.crypto;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class FactoryUtil {

    public static KeyFactory createKeyFactory(String algorithm) {
        try {
            return KeyFactory.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("required " + algorithm + " key factory not supported", e);
        }
    }

    public static CertificateFactory createCertificateFactory(String type) {
        try {
            return CertificateFactory.getInstance(type);
        } catch (CertificateException e) {
            throw new RuntimeException("required " + type + " certificate factory not supported", e);
        }
    }

}
