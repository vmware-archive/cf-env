package io.pivotal.labs.cfenv.crypto;

import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public enum KeySense {
    PUBLIC {
        @Override
        public KeySpec parseKey(KeyFormat format, KeyAlgorithm algorithm, byte[] keyBytes) throws IOException {
            return format.parsePublicKey(algorithm, keyBytes);
        }

        @Override
        protected Key generate(KeyFactory keyFactory, KeySpec spec) throws InvalidKeySpecException {
            return keyFactory.generatePublic(spec);
        }
    },
    PRIVATE {
        @Override
        public KeySpec parseKey(KeyFormat format, KeyAlgorithm algorithm, byte[] keyBytes) throws IOException {
            return format.parsePrivateKey(algorithm, keyBytes);
        }

        @Override
        protected Key generate(KeyFactory keyFactory, KeySpec spec) throws InvalidKeySpecException {
            return keyFactory.generatePrivate(spec);
        }
    };

    public abstract KeySpec parseKey(KeyFormat format, KeyAlgorithm algorithm, byte[] keyBytes) throws IOException;

    public Key generate(KeyAlgorithm algorithm, KeySpec spec) throws InvalidKeySpecException {
        return generate(algorithm.getFactory(), spec);
    }

    protected abstract Key generate(KeyFactory keyFactory, KeySpec spec) throws InvalidKeySpecException;

}
