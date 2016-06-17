package io.pivotal.labs.cfenv.crypto;

import java.security.KeyFactory;

public class ECKeyFactory {
    public static final KeyFactory INSTANCE = FactoryUtil.createKeyFactory("EC");
}
