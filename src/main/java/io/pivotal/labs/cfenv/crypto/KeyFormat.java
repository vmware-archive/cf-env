package io.pivotal.labs.cfenv.crypto;

import java.io.IOException;
import java.security.spec.KeySpec;

public enum KeyFormat {
    /**
     * PKCS#8 private keys and X.509 public keys.
     */
    NATIVE {
        @Override
        public KeyAlgorithm determineAlgorithm(String algorithmName, byte[] keyBytes) {
            return KeyAlgorithm.determineAlgorithm(keyBytes);
        }

        @Override
        public KeySpec parse(KeyAlgorithm algorithm, KeySense sense, byte[] keyBytes) throws IOException {
            return sense.parseNativeSpec(keyBytes);
        }
    },
    /**
     * PKCS#1/SSLeay private keys - no public keys yet!
     */
    LEGACY {
        @Override
        public KeyAlgorithm determineAlgorithm(String algorithmName, byte[] keyBytes) {
            return KeyAlgorithm.valueOf(algorithmName);
        }

        @Override
        public KeySpec parse(KeyAlgorithm algorithm, KeySense sense, byte[] keyBytes) throws IOException {
            return algorithm.parseLegacyPrivateKeySpec(keyBytes);
        }
    };

    public abstract KeyAlgorithm determineAlgorithm(String algorithmName, byte[] keyBytes);

    public abstract KeySpec parse(KeyAlgorithm algorithm, KeySense sense, byte[] keyBytes) throws IOException;

}
