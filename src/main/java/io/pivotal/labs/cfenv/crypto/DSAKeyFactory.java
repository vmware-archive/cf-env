package io.pivotal.labs.cfenv.crypto;

import java.security.KeyFactory;

public class DSAKeyFactory {
    public static final KeyFactory INSTANCE = FactoryUtil.createKeyFactory("DSA");
}
