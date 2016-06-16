package io.pivotal.labs.cfenv;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class X509CertificateFactory {

    public static final CertificateFactory INSTANCE = createCertificateFactory();

    private static CertificateFactory createCertificateFactory() {
        try {
            return CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            throw new RuntimeException("required X.509 certificate factory not supported", e);
        }
    }

}
