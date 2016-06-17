package io.pivotal.labs.cfenv.crypto;

import java.security.cert.CertificateFactory;

public class X509CertificateFactory {
    public static final CertificateFactory INSTANCE = FactoryUtil.createCertificateFactory("X.509");
}
