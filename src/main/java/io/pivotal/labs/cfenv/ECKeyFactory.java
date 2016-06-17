package io.pivotal.labs.cfenv;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;

public class ECKeyFactory {

    public static final KeyFactory INSTANCE = createKeyFactory();

    private static KeyFactory createKeyFactory() {
        try {
            return KeyFactory.getInstance("EC");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("required EC key factory not supported", e);
        }
    }

}
