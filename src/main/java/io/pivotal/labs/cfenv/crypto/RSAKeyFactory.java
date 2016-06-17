package io.pivotal.labs.cfenv.crypto;

import java.security.KeyFactory;

public class RSAKeyFactory {
    public static final KeyFactory INSTANCE = FactoryUtil.createKeyFactory("RSA");
}
