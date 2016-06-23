package io.pivotal.labs.cfenv.crypto;

import java.io.IOException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

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
        public KeySpec parsePublicKey(KeyAlgorithm algorithm, byte[] keyBytes) throws IOException {
            return new X509EncodedKeySpec(keyBytes);
        }

        @Override
        public KeySpec parsePrivateKey(KeyAlgorithm algorithm, byte[] keyBytes) throws IOException {
            return new PKCS8EncodedKeySpec(keyBytes);
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
        public KeySpec parsePublicKey(KeyAlgorithm algorithm, byte[] keyBytes) throws IOException {
            return algorithm.parseLegacyPublicKey(keyBytes);
        }

        @Override
        public KeySpec parsePrivateKey(KeyAlgorithm algorithm, byte[] keyBytes) throws IOException {
            return algorithm.parseLegacyPrivateKey(keyBytes);
        }
    };

    public abstract KeyAlgorithm determineAlgorithm(String algorithmName, byte[] keyBytes);

    public abstract KeySpec parsePublicKey(KeyAlgorithm algorithm, byte[] keyBytes) throws IOException;

    public abstract KeySpec parsePrivateKey(KeyAlgorithm algorithm, byte[] keyBytes) throws IOException;

}
