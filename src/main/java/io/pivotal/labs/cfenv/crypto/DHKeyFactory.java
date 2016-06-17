package io.pivotal.labs.cfenv.crypto;

import java.security.KeyFactory;

public class DHKeyFactory {
    public static final KeyFactory INSTANCE = FactoryUtil.createKeyFactory("DiffieHellman");
}
