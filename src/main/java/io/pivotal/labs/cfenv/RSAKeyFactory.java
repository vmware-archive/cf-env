package io.pivotal.labs.cfenv;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

public class RSAKeyFactory {

    public static final KeyFactory INSTANCE = createKeyFactory();

    private static KeyFactory createKeyFactory() {
        try {
            return KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("required RSA key factory not supported", e);
        }
    }

}
