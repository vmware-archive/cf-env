package io.pivotal.labs.cfenv.crypto;

import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public enum KeySense {
    PUBLIC {
        @Override
        public KeySpec parseNativeSpec(byte[] keyBytes) {
            return new X509EncodedKeySpec(keyBytes);
        }

        @Override
        public Key generate(KeyFactory keyFactory, KeySpec spec) throws InvalidKeySpecException {
            return keyFactory.generatePublic(spec);
        }
    },
    PRIVATE {
        @Override
        public KeySpec parseNativeSpec(byte[] keyBytes) {
            return new PKCS8EncodedKeySpec(keyBytes);
        }

        @Override
        public Key generate(KeyFactory keyFactory, KeySpec spec) throws InvalidKeySpecException {
            return keyFactory.generatePrivate(spec);
        }
    };

    public abstract KeySpec parseNativeSpec(byte[] keyBytes);

    public abstract Key generate(KeyFactory keyFactory, KeySpec spec) throws InvalidKeySpecException;

}
